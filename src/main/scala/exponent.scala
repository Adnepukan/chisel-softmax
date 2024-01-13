import chisel3._
import chisel3.util._
import chisel3.experimental._
import scala.math._

class Exponent(
    inW: Int,
    inBP: Int,
    outW: Int,
    outBP: Int,
    n: Int,
    facBP: Int = 3
) extends Module{
    val io = IO(new Bundle{
        val input = Flipped(Decoupled(new VecFixed(n, inW, inBP)))
        val output = Decoupled(new VecFixed(n, outW, outBP))
    })

    // Valid-Ready Control
    val hsk = Module(new PipelineHsk(2))
    hsk.io.validPre := io.input.valid
    io.input.ready := hsk.io.readyPre
    io.output.valid := hsk.io.validNxt
    hsk.io.readyNxt := io.output.ready
    hsk.io.lastPre := io.input.bits.last
    io.output.bits.last := hsk.io.lastNxt

    // Dataflow
    val log2e = (1/log(2)).F((facBP+2).W, facBP.BP)
    val data1 = io.input.bits.x.map(x => RegEnable(x*log2e, hsk.io.regen(0)))
    val data1Int = data1.map(_(facBP+inW+1, facBP+inBP))
    val data1Frac = data1.map(_(facBP+inBP-1, 0))

    val lutSize = 1<<(facBP+inBP)
    val lut = Wire(Vec(lutSize, FixedPoint(outW.W, outBP.BP)))
    lut := lut.zipWithIndex.map({case (x, i) => pow(2, -i.toDouble/lutSize.toDouble).F(outW.W, outBP.BP)})
    val data2 = data1Frac.zipWithIndex
        .map({case (x, i) => RegEnable((lut(x)>>data1Int(i))(outW-1, 0).asFixedPoint(outBP.BP), hsk.io.regen(1))})

    io.output.bits.x := data2
}

object Exponent extends App{
    println(getVerilogString(new Exponent(8, 4, 16, 8, 1, 4)))
}
