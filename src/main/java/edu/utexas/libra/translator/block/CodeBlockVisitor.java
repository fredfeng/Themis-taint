package edu.utexas.libra.translator.block;

public interface CodeBlockVisitor<T> {
    T visit(SeqCodeBlock seqBlock);
    T visit(BranchCodeBlock branchBlock);
    T visit(LoopCodeBlock loopBlock);
}
