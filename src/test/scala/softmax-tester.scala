import chisel3._
import chisel3.util._
import chisel3.experimental._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SoftmaxTest extends AnyFlatSpec with ChiselScalatestTester{
    behavior of "Softmax"
    it should "pass" in {
        test(new Softmax(8, 4, 8, 6, 16, 1)) { dut =>
            dut.clock.setTimeout(0)
            dut.clock.step(10)
            dut.io.output.ready.poke(true.B)
            dut.io.input.valid.poke(true.B)
            dut.io.input.bits.x.zipWithIndex.map({case (x, i) => x.poke((i/4).F(8.W, 4.BP))})
            dut.io.input.bits.last.poke(true.B)
            dut.clock.step(1)
            dut.io.input.valid.poke(false.B)
            dut.clock.step(13)
            println("out="+dut.io.output.bits.x.peek().toString)
            println("outlast="+dut.io.output.bits.last.peek().toString)
            println("outvalid="+dut.io.output.valid.peek().toString)
            println("\nEnd Testing Softmax.")
        }
    }
}
