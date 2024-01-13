import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ExponentTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "Exponent"
    it should "pass" in {
        test(new Exponent(8, 4, 16, 8, 1, 4)) { dut =>
            dut.clock.setTimeout(0)
            dut.clock.step(10)
            for (i <- 0 until 100) {
                dut.io.output.ready.poke(true.B)
                dut.io.input.valid.poke(true.B)
                dut.io.input.bits.x(0).poke(1.F(8.W, 4.BP))
                dut.io.input.bits.last.poke(false.B)
                dut.clock.step(3)
                dut.io.output.ready.poke(true.B)
                dut.io.output.bits.x(0).expect(0.3671875.F(16.W, 8.BP))
                dut.io.output.valid.expect(true.B)
                dut.io.input.ready.expect(true.B)
                dut.io.output.bits.last.expect(false.B)
                dut.clock.step(3)
            }
            println("\nEnd Testing Exponent.")
        }
    }
}
