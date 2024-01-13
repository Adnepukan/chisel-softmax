import chisel3._
import chisel3.util._
import chisel3.experimental._
import scala.math._

class Logarithm(
    inW: Int,
    inBP: Int,
    outW: Int,
    outBP: Int,
    n: Int
) extends Module{
    val io = IO(new Bundle{
        val input = Flipped(Decoupled(new VecFixed(n, inW, inBP)))
        val output = Decoupled(new VecFixed(n, outW, outBP))
    })

    // Valid-Ready Control
    val hsk = Module(new PipelineHsk(4))
    hsk.io.validPre := io.input.valid
    io.input.ready := hsk.io.readyPre
    hsk.io.lastPre := io.input.bits.last
    io.output.valid := hsk.io.validNxt
    hsk.io.readyNxt := io.output.ready
    io.output.bits.last := hsk.io.lastNxt

    // Dataflow
    val lod = Wire(Vec(n, Vec(inW+1, Bool())))
    for(i<-0 until n){
        lod(i)(inW) := (io.input.bits.x(i)(inW-1))
        lod(i)(0) := io.input.bits.x(i)===0.F(inW.W, inBP.BP)
        for(j<-1 until inW)
            lod(i)(j) := ~(io.input.bits.x(i)(inW-1, j).asBools.fold(0.B)(_ | _)) & io.input.bits.x(i)(j-1)
    }
    val data1Lod = RegEnable(lod, hsk.io.regen(0))
    val data1In = RegEnable(io.input.bits.x, hsk.io.regen(0))

    val countWidth = (log(inW-1)/log(2)+2).toInt
    val data2InMux = Wire(Vec(n, FixedPoint(inW.W, inBP.BP)))
    val data2CountMux = Wire(Vec(n, SInt(countWidth.W)))
    for(i<-0 until n)
        data2InMux(i) := data1Lod(i).zipWithIndex
            .map({case (x, j) => Mux(x, (data1In(i)<<(inW-j))(inW-1, 0), 0.U(inW.W))})
            .fold(0.U(inW.W))(_ | _).asFixedPoint(inBP.BP)
    data2CountMux := data1Lod.map(y => y.zipWithIndex
        .map({case (x, i) => Mux(x, (i-1-inBP).S(countWidth.W), 0.S(countWidth.W))})
        .fold(0.S(countWidth.W))(_ | _))
    val data2In = RegEnable(data2InMux, hsk.io.regen(1))
    val data2Count = RegEnable(data2CountMux, hsk.io.regen(1))

    val lutSize = 1<<(inW-1)
    val lut = Wire(Vec(lutSize, FixedPoint((outBP+1).W, outBP.BP)))
    lut := lut.zipWithIndex
        .map({case (x, i) => (log(1+i/lutSize.toDouble)).F((outBP+1).W, outBP.BP)})
    val data3In1 = data2In.map(x => RegEnable(lut(x(inW-2, 0)), hsk.io.regen(2)))
    val data3In2 = data2Count.map(x => RegEnable(log(2).F((outBP+1).W, outBP.BP)*x.asFixedPoint(0.BP), hsk.io.regen(2)))

    val data4 = data3In1.zipWithIndex.map({ case (x, i) => RegEnable(x+data3In2(i), hsk.io.regen(3))})
    io.output.bits.x := data4
}

object Logarithm extends App{
    println(getVerilogString(new Logarithm(8, 4, 12, 8, 1)))
}
