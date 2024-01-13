import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LogarithmTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "Logarithm"
    it should "pass" in {
        test(new Logarithm(8, 4, 12, 8, 1)) { dut =>
            dut.clock.setTimeout(0)
            dut.clock.step(10)
            for (i <- 0 until 100) {
                dut.io.output.ready.poke(true.B)
                dut.io.input.valid.poke(true.B)
                dut.io.input.bits.x(0).poke(4.375.F(8.W, 4.BP))
                dut.io.input.bits.last.poke(false.B)
                dut.clock.step(4)
                dut.io.output.ready.poke(true.B)
                dut.io.output.bits.x(0).expect(1.4726562.F(16.W, 8.BP))
                dut.io.output.valid.expect(true.B)
                dut.io.input.ready.expect(true.B)
                dut.io.output.bits.last.expect(false.B)
                dut.clock.step(4)
            }
            println("\nEnd Testing Logarithm.")
        }
    }
}
