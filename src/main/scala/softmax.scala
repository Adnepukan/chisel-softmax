import chisel3._
import chisel3.util._
import chisel3.experimental._
import scala.math._
import java.io.PrintWriter

class Softmax(
    inW: Int,
    inBP: Int,
    outW: Int,
    outBP: Int,
    n: Int,
    depth: Int,
    expFacBP: Int = 3,
    treeRtl: Int = 1
) extends Module{
    val adderTreeWidth = outW+(log(n-1)/log(2)).toInt+1
    val logarithmWidth = outW+(log(n*depth-1)/log(2)).toInt+1

    val io = IO(new Bundle{
        val input = Flipped(Decoupled(new VecFixed(n, inW, inBP)))
        val output = Decoupled(new VecFixed(n, outW, outBP))
    })
    val rst = IO(Input(AsyncReset()))

    withReset(rst){
        // control
        val inPathSelNxt = Wire(Bool())
        val inPathSelEn = Wire(Bool())
        val outPathSelNxt = Wire(Bool())
        val outPathSelEn = Wire(Bool())
        val inPathSel = RegEnable(inPathSelNxt, 0.B, inPathSelEn)
        val outPathSel = RegEnable(outPathSelNxt, 0.B, outPathSelEn)
        inPathSelNxt := ~inPathSel
        outPathSelNxt := ~outPathSel

        // modules
        val exponentArray = Module(new Exponent(inW, inBP, outW, outBP, n, expFacBP))
        val exponentArrayInHsk = Module(new Hsk1ton(2, exponentArray.io.input.bits.cloneType))
        val exponentArrayInMux = Module(new Arbiternto1(2, exponentArray.io.input.bits.cloneType))
        val exponentArrayOutMux = Module(new Arbiter1ton(2, exponentArray.io.output.bits.cloneType))
        val queue = Module(new Queue(exponentArray.io.input.bits.cloneType, depth))
        val adder = Module(new Adder2to1(inW, inBP, n, false, true))
        val adderTree = Module(new AdderTree(adderTreeWidth, outBP, n, treeRtl))
        val accumulator = Module(new Accumulator2to1(logarithmWidth, outBP))
        val logarithm = Module(new Logarithm(logarithmWidth, outBP, inW, inBP, 1))
        val queueOutHsk = Module(new Hsknto1(2, exponentArray.io.input.bits.cloneType))

        // exponentArrayInMux
        VecFixed.depart(io.input, exponentArrayInHsk.io.input)
        exponentArrayInHsk.io.output(0) <> exponentArrayInMux.io.input(0)
        exponentArrayInHsk.io.output(1) <> queue.io.enq
        adder.io.output <> exponentArrayInMux.io.input(1)
        exponentArrayInMux.io.output <> exponentArray.io.input

        // exponentArrayOutMux
        exponentArray.io.output <> exponentArrayOutMux.io.input
        exponentArrayOutMux.io.output(0) <> adderTree.io.input
        exponentArrayOutMux.io.output(1) <> io.output

        // AdderTree
        adderTree.io.output <> accumulator.io.input
        accumulator.io.output <> logarithm.io.input
        queue.io.deq <> queueOutHsk.io.input(0)
        VecFixed.repeat(logarithm.io.output, queueOutHsk.io.input(1))
        VecFixed.combine(queueOutHsk.io.output, adder.io.input, 0)

        // Mux
        exponentArrayInMux.io.sel := inPathSel
        inPathSelEn := exponentArray.io.input.valid&exponentArray.io.input.ready&exponentArray.io.input.bits.last
        exponentArrayOutMux.io.sel := outPathSel
        outPathSelEn := exponentArray.io.output.valid&exponentArray.io.output.ready&exponentArray.io.output.bits.last
    }
}

object Softmax extends App{
     new PrintWriter("vivado/softmax.v"){
         write(getVerilogString(new Softmax(8, 4, 8, 6, 16, 1)))
         close()
     }
}
