import chisel3._
import chisel3.util._
import chisel3.experimental._
import scala.math._

class AdderTree(
    outW: Int,
    outBP: Int,
    n: Int,
    rtl: Int = 1 // Register Transfer Lantency
) extends Module{
    val io = IO(new Bundle{
        val input = Flipped(Decoupled(new VecFixed(n, outW, outBP)))
        val output = Decoupled(new VecFixed(1, outW, outBP))
    })

    // Select the best parameters
    var level32 = 0
    var level42 = 0
    while(pow(2, level42)<n){
        level42 = level42+1
    }
    while(pow(2, level42-2)*pow(3, level32+1)>=n){
        level42 = level42-2
        level32 = level32+1
    }
    level42 = level42+level32
    val ntotal = (pow(2, level42-level32)*pow(3, level32)).toInt

    // Valid-Ready Control
    val level32rest = (2*rtl)-level32%(2*rtl)
    val hsk = Module(new PipelineHsk(level32/(2*rtl)+(level42-level32rest/2+(rtl-1))/rtl+1))
    hsk.io.validPre := io.input.valid
    io.input.ready := hsk.io.readyPre
    hsk.io.lastPre := io.input.bits.last
    io.output.valid := hsk.io.validNxt
    hsk.io.readyNxt := io.output.ready
    io.output.bits.last := hsk.io.lastNxt

    // Dataflows
    val sumIn = Wire(Vec(level32+level42+1, Vec(ntotal, FixedPoint(outW.W, outBP.BP))))
    val sumOut = Wire(sumIn.cloneType)

    for(i<-0 to level32)
        if(i%(2*rtl)==0 && i!=0)
            sumOut(i) := sumIn(i).map(RegEnable(_, hsk.io.regen(i/(2*rtl)-1)))
        else
            sumOut(i) := sumIn(i)
    for(i<-1 to level42)
        if(i%rtl==(level32rest%(2*rtl))/2)
            sumOut(i+level32) := sumIn(i+level32).map(RegEnable(_, hsk.io.regen(level32/(2*rtl)-1+(i+rtl-1)/rtl)))
        else if(i==level42)
            sumOut(i+level32) := sumIn(i+level32).map(RegEnable(_, hsk.io.regen((level32/(2*rtl)+(level42-(((2*rtl)-level32%(2*rtl))%(2*rtl))/2-1)/(2*rtl)))))
        else
            sumOut(i+level32) := sumIn(i+level32)

    for(i<-0 until level32){
        var delta = (pow(2, level42-level32+i)*pow(3, level32-1-i)).toInt
        for(j<-0 until delta){
            sumIn(i+1)(j) := (sumOut(i)(j).asUInt^sumOut(i)(j+delta).asUInt^sumOut(i)(j+delta*2).asUInt).asFixedPoint(outBP.BP)
            sumIn(i+1)(j+delta) := ((sumOut(i)(j).asUInt&sumOut(i)(j+delta).asUInt|sumOut(i)(j).asUInt&sumOut(i)(j+delta*2).asUInt|sumOut(i)(j+delta).asUInt&sumOut(i)(j+delta*2).asUInt).asFixedPoint(outBP.BP))<<1
            sumIn(i+1)(j+delta*2) := 0.F(outW.W, outBP.BP)
        }
        for(j<-(delta*3) until ntotal)
            sumIn(i+1)(j) := 0.F(outW.W, outBP.BP)
    }
    for(i<-0 until level42-1){
        var delta = pow(2, level42-2-i).toInt
        for(j<-0 until delta){
            val s = Wire(UInt(outW.W))
            val c = Wire(UInt(outW.W))
            s := (sumOut(i+level32)(j).asUInt^sumOut(i+level32)(j+delta).asUInt^sumOut(i+level32)(j+delta*2).asUInt)
            c := (sumOut(i+level32)(j).asUInt&sumOut(i+level32)(j+delta).asUInt|sumOut(i+level32)(j).asUInt&sumOut(i+level32)(j+delta*2).asUInt|sumOut(i+level32)(j+delta).asUInt&sumOut(i+level32)(j+delta*2).asUInt)<<1
            sumIn(i+level32+1)(j) := (s^c^sumOut(i+level32)(j+delta*3).asUInt).asFixedPoint(outBP.BP)
            sumIn(i+level32+1)(j+delta) := ((s&c | s&sumOut(i+level32)(j+delta*3).asUInt | c&sumOut(i+level32)(j+delta*3).asUInt).asFixedPoint(outBP.BP))<<1
            sumIn(i+level32+1)(j+delta*2) := 0.F(outW.W, outBP.BP)
            sumIn(i+level32+1)(j+delta*3) := 0.F(outW.W, outBP.BP)
        }
        for(j<-(delta*4) until ntotal)
            sumIn(i+level32+1)(j) := 0.F(outW.W, outBP.BP)
    }

    for(j<-0 until n)
        sumIn(0)(j) := io.input.bits.x(j)
    for(j<-n until ntotal)
        sumIn(0)(j) := 0.F(outW.W, outBP.BP)

    sumIn(level32+level42)(0) := sumOut(level32+level42-1)(0) + sumOut(level32+level42-1)(1)
    for(j<-1 until ntotal)
        sumIn(level32+level42)(j) := 0.F(outW.W, outBP.BP)

    io.output.bits.x(0) := sumOut(level32+level42)(0)
};

object AdderTree extends App{
    println(getVerilogString(new AdderTree(9, 4, 2, 1)))
}
