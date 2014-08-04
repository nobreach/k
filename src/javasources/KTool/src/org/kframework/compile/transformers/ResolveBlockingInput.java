// Copyright (c) 2012-2014 K Team. All Rights Reserved.
package org.kframework.compile.transformers;

import org.kframework.compile.utils.KilProperty;
import org.kframework.compile.utils.GetLhsPattern;
import org.kframework.compile.utils.MetaK;
import org.kframework.kil.*;
import org.kframework.kil.Cell.Ellipses;
import org.kframework.kil.loader.Context;
import org.kframework.kil.visitors.BasicVisitor;
import org.kframework.kil.visitors.CopyOnWriteTransformer;
import org.kframework.utils.errorsystem.KException;
import org.kframework.utils.errorsystem.KException.ExceptionType;
import org.kframework.utils.errorsystem.KException.KExceptionGroup;
import org.kframework.utils.general.GlobalSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This compilation phase adds rules for fetching new input from the input stream whenever such input is needed.
 * To achieve this, rules reading from cells tagged with "stdin" are detected, and for each such rule
 * another rule is generated which when the input list contains no elements to be read issues a request that a
 * token should be read from stdin and parsed into the requested type.
 *
 * For example, for rule
 *   rule <k> read => I ...</k> <in> ListItem(I:Int) => .List ...</in>
 * this rule will also be generated:
 *   rule <k> read ...</k> <in> (.List => ListItem(parseInput("Int", " \n\r\t"))) ListItem(#buffer(K))</in>o
 *
 * The current implementation assumes this pass runs after concrete syntax has been compiled away.
 */
@KilProperty.Requires(KilProperty.NO_CONCRETE_SYNTAX)
public class ResolveBlockingInput extends GetLhsPattern {

    Map<String, String> inputCells = new HashMap<String, String>();
    java.util.List<Rule> generated = new ArrayList<Rule>();
    boolean hasInputCell;
    /**
     * The original requires condition of the rule.  the predicate  corresponding to the variable being
     * read from the stream will be removed from this condition before setting this condition as the
     * condition of the new generated rule.
     * Initialized in transform(Rule).
     * Altered in getSort(Variable).
     */
    private Term originalCondition;
    /**
     * The resulting condition is obtained from the originalCondition by adding a check that the
     * variable being read does not match the stream handle.
     * Initialized in transform(Cell).
     */
    Term resultCondition;

    public ResolveBlockingInput(Context context) {
        super("Resolve Blocking Input", context);
    }

    @Override
    public ASTNode visit(Definition node, Void _)  {
        Configuration config = MetaK.getConfiguration(node, context);
        new BasicVisitor(context) {
            @Override
            public Void visit(Cell node, Void _) {
                String stream = node.getCellAttributes().get("stream");
                if ("stdin".equals(stream)) {
                    String delimiter = node.getCellAttributes().get("delimiters");
                    if (delimiter == null) {
                        delimiter = " \n\t\r";
                    }
                    inputCells.put(node.getLabel(), delimiter);
                }
                return super.visit(node, _);
            }

        }.visitNode(config);
        return super.visit(node, _);
    }

    @Override
    public ASTNode visit(Module node, Void _)  {
        ASTNode result = super.visit(node, _);
        if (result != node) {
            GlobalSettings.kem.register(new KException(ExceptionType.ERROR,
                    KExceptionGroup.INTERNAL,
                    "Should have obtained the same module.",
                    getName(), node.getFilename(), node.getLocation()));
        }
        if (generated.isEmpty()) return node;
        node = node.shallowCopy();
        node.getItems().addAll(generated);
        return node;
    }

    @Override
    public ASTNode visit(Configuration node, Void _)  {
        return node;
    }

    @Override
    public ASTNode visit(org.kframework.kil.Context node, Void _)  {
        return node;
    }

    @Override
    public ASTNode visit(Syntax node, Void _)  {
        return node;
    }

    @Override
    public ASTNode visit(Rule node, Void _)  {
        hasInputCell = false;
        if (node.getAttributes().containsKey("stdin")) {
            // a rule autogenerated by AddStreamCells, so we shouldn't touch it.
            return node;
        }
        originalCondition = node.getRequires();
        ASTNode resultNode = super.visit(node, _);
        if (hasInputCell) {
            Rule result = (Rule) resultNode;
            result.setRequires(originalCondition);
            generated.add(result);
            node.setRequires(resultCondition);
        }
        return node;
    }

    @Override
    public ASTNode visit(Cell node, Void _)  {
        if ((!inputCells.containsKey(node.getLabel()))) {
            return super.visit(node, _);
        }
        if (!(node.getEllipses() == Ellipses.RIGHT)) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING,
                    KExceptionGroup.COMPILER,
                    "cell should have right ellipses but it doesn't." +
                            System.getProperty("line.separator") + "Won't transform.",
                            getName(), node.getFilename(), node.getLocation()));
            return node;
        }
        Term contents = node.getContents();
        if (!(contents instanceof Rewrite)) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING,
                    KExceptionGroup.COMPILER,
                    "Expecting a rewrite of a basic type variable into the empty list but got " + contents.getClass() + "." +
                            System.getProperty("line.separator") + "Won't transform.",
                            getName(), contents.getFilename(), contents.getLocation()));
            return node;
        }
        Rewrite rewrite = (Rewrite) contents;
        if ((
            (!(rewrite.getLeft() instanceof KApp &&
            ((KApp)rewrite.getLeft()).getLabel().equals(
                KLabelConstant.of(DataStructureSort.DEFAULT_LIST_ITEM_LABEL, context)))))) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING,
                    KExceptionGroup.COMPILER,
                    "Expecting a list item but got " + rewrite.getLeft().getClass() + "." +
                            System.getProperty("line.separator") + "Won't transform.",
                            getName(), rewrite.getLeft().getFilename(), rewrite.getLeft().getLocation()));
            return node;
        }
        Term item = rewrite.getLeft();
        Term variable;
        KApp kappItem = (KApp)item;
        Term child = kappItem.getChild();
        if (!(child instanceof KList) || ((KList)child).getContents().size() != 1) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING,
                KExceptionGroup.COMPILER,
                "Expecting an input type variable but got a KList instead. Won't transform.",
                        getName(), ((KApp)item).getChild().getFilename(), ((KApp)item).getChild().getLocation()));
            return node;
        }
        variable = ((KList)child).getContents().get(0);

        if (!(variable instanceof Variable))//&&    MetaK.isBuiltinSort(item.getItem().getSort())
                 {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING,
                    KExceptionGroup.COMPILER,
                    "Expecting an input type variable but got " + variable.getClass() + "." +
                            System.getProperty("line.separator") + "Won't transform.",
                            getName(), variable.getFilename(), variable.getLocation()));
            return node;
        }
        if ((!(rewrite.getRight() instanceof KApp &&
            ((KApp)rewrite.getRight()).getLabel().equals(
                KLabelConstant.of(DataStructureSort.DEFAULT_LIST_UNIT_LABEL, context))))) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING,
                    KExceptionGroup.COMPILER,
                    "Expecting an empty list but got " + rewrite.getRight().getClass() + " of sort " +
                            rewrite.getRight().getSort() + "." +
                            System.getProperty("line.separator") + "Won't transform.",
                            getName(), rewrite.getRight().getFilename(), rewrite.getRight().getLocation()));
            return node;
        }

        hasInputCell = true;
        resultCondition = MetaK.incrementCondition(originalCondition, getPredicateTerm((Variable) variable));

        Sort sort = getSort((Variable) variable);
        Term parseTerm = KApp.of(parseInputLabel,
            StringBuiltin.kAppOf(sort.getName()),
            StringBuiltin.kAppOf(inputCells.get(node.getLabel())));

        Term ioBuffer = KApp.of(bufferLabel,
           new Variable(Variable.getFreshVar(Sort.K)));

//        ctor(List)[replaceS[emptyCt(List),parseTerm(string(Ty),nilK)],ioBuffer(mkVariable('BI,K))]
        Term list;
        DataStructureSort myList = context.dataStructureListSortOf(
            DataStructureSort.DEFAULT_LIST_SORT);
        Term term1 = new Rewrite(
            KApp.of(KLabelConstant.of(myList.unitLabel(), context)),
            KApp.of(KLabelConstant.of(myList.elementLabel(), context), parseTerm),
            context);
        Term term2 = KApp.of(KLabelConstant.of(myList.elementLabel(), context), ioBuffer);
        list = KApp.of(KLabelConstant.of(myList.constructorLabel(), context), term1, term2);


        node = node.shallowCopy();
        node.setContents(list);
        return node;
    }

    /**
     * This method gets the *concrete* sort of variable var. As the ResolveBlocking input phase is run
     * after the syntax is flatten, a rule like
     *   rule <k> read => I ...</k> <in> ListItem(I:Int) => .List ...</in>
     * will become
     *   rule <k> 'read(.KList) => I ...</k> <in> ListItem(I:KItem) => .List ...</in> when isInt(I)
     *
     * Thus, the sort of the variable is moved into the side condition.  This method retrieves the
     * sort of the variable from the side condition (stored in the originalCondition field) and removes the
     * corresponding predicate from the side condition to be used for the newly generated rule.
     *
     * @modifies originalCondition field
     * @param var the variable to be looked up
     * @return the original sort of var.
     * @throws TransformerException
     */

    private Sort getSort(final Variable var) {
        if (!var.getSort().equals(Sort.KITEM)) return var.getSort();
        final String[] sort = {null};
        CopyOnWriteTransformer transformer = new CopyOnWriteTransformer("find missing variables", context) {

            @Override
            public ASTNode visit(KApp node, Void _) {
                if (node.getChild() instanceof KList) {
                    KList args = (KList) node.getChild();
                    if (args.getContents().size() == 1) {
                        Term v = args.getContents().get(0);
                        if (var.equals(v)) {
                            assert node.getLabel() instanceof KLabelConstant : "label should be a predicate label";
                            KLabelConstant l = (KLabelConstant) node.getLabel();
                            assert l.isPredicate() : "label should be a predicate label";
                            sort[0] = l.getLabel().substring(2);
                            return null;
                        }
                    }
                }
                return super.visit(node, _);
            }
        };
        originalCondition = (Term) transformer.visitNode(originalCondition);

        return Sort.of(sort[0]);
    }

    private static final KLabelConstant parseInputLabel = KLabelConstant.of("'#parseInput");
    private static final KLabelConstant bufferLabel = KLabelConstant.of("'#buffer");

    private Term getPredicateTerm(Variable var) {
        return KApp.of(KLabelConstant.KNEQ_KLABEL, KApp.of(KLabelConstant.STREAM_PREDICATE, var), BoolBuiltin.TRUE);
    }
}
