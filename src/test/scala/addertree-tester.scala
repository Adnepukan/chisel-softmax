import chisel3._
import chisel3.util._
import chisel3.experimental._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AdderTreeTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "AdderTree"
    it should "pass" in {
        test(new AdderTree(9, 4, 2, 1)) { dut =>
            dut.clock.setTimeout(0)
            for (_ <- 0 until 100) {
                dut.io.output.ready.poke(true.B)
                dut.io.input.valid.poke(true.B)
                for(i<-0 until 2)
                    dut.io.input.bits.x(i).poke(0.5.F(9.W, 4.BP))
                dut.io.input.bits.last.poke(false.B)
                dut.clock.step(1)
                dut.io.output.ready.poke(true.B)
                dut.io.output.bits.x(0).expect(1.F(9.W, 4.BP))
                dut.io.input.ready.expect(true.B)
                dut.io.output.valid.expect(true.B)
                dut.io.output.bits.last.expect(false.B)
                dut.clock.step(1)
            }
            println("\nEnd Testing AdderTree.")
        }
    }
}
