circuit AdderTree :
  module PipelineHsk :
    input clock : Clock
    input reset : UInt<1>
    input io_validPre : UInt<1>
    output io_readyPre : UInt<1>
    input io_lastPre : UInt<1>
    output io_validNxt : UInt<1>
    input io_readyNxt : UInt<1>
    output io_regen_0 : UInt<1>
    output io_lastNxt : UInt<1>
    input rst : AsyncReset

    reg valid_1_r : UInt<1>, clock with :
      reset => (rst, UInt<1>("h0")) @[Reg.scala 28:20]
    node ready_1 = io_readyNxt @[pipelinehsk.scala 19:21 32:22]
    node valid_1 = valid_1_r @[pipelinehsk.scala 18:21 24:24]
    node _ready_0_T = not(valid_1) @[pipelinehsk.scala 26:38]
    node _ready_0_T_1 = or(ready_1, _ready_0_T) @[pipelinehsk.scala 26:36]
    node ready_0 = _ready_0_T_1 @[pipelinehsk.scala 19:21 26:22]
    node valid_0 = io_validPre @[pipelinehsk.scala 18:21 29:18]
    node _GEN_0 = mux(ready_0, valid_0, valid_1_r) @[Reg.scala 29:18 28:20 29:22]
    reg last_1_r : UInt<1>, clock with :
      reset => (rst, UInt<1>("h0")) @[Reg.scala 28:20]
    node last_0 = io_lastPre @[pipelinehsk.scala 20:20 33:17]
    node _GEN_1 = mux(io_regen_0, last_0, last_1_r) @[Reg.scala 29:18 28:20 29:22]
    node _io_regen_0_T = and(valid_0, ready_0) @[pipelinehsk.scala 27:37]
    node last_1 = last_1_r @[pipelinehsk.scala 20:20 25:23]
    io_readyPre <= ready_0 @[pipelinehsk.scala 30:21]
    io_validNxt <= valid_1 @[pipelinehsk.scala 31:21]
    io_regen_0 <= _io_regen_0_T @[pipelinehsk.scala 27:25]
    io_lastNxt <= last_1 @[pipelinehsk.scala 34:20]
    valid_1_r <= _GEN_0 @[Reg.scala 28:{20,20}]
    last_1_r <= _GEN_1 @[Reg.scala 28:{20,20}]

  module AdderTree :
    input clock : Clock
    input reset : UInt<1>
    output io_input_ready : UInt<1>
    input io_input_valid : UInt<1>
    input io_input_bits_in_0 : SInt<9>
    input io_input_bits_in_1 : SInt<9>
    input io_input_bits_last : UInt<1>
    input io_output_ready : UInt<1>
    output io_output_valid : UInt<1>
    output io_output_bits_out : SInt<9>
    output io_output_bits_last : UInt<1>
    input rst : AsyncReset

    inst hsk of PipelineHsk @[addertree.scala 39:21]
    reg r : SInt<9>, clock with :
      reset => (UInt<1>("h0"), r) @[Reg.scala 16:16]
    node sumIn_0_0 = io_input_bits_in_0 @[addertree.scala 49:21 92:21]
    node sumOut_0_0 = sumIn_0_0 @[addertree.scala 50:22 56:23]
    node sumIn_0_1 = io_input_bits_in_1 @[addertree.scala 49:21 92:21]
    node sumOut_0_1 = sumIn_0_1 @[addertree.scala 50:22 56:23]
    node _sumIn_1_0_T = add(sumOut_0_0, sumOut_0_1) @[addertree.scala 96:63]
    node _sumIn_1_0_T_1 = tail(_sumIn_1_0_T, 1) @[addertree.scala 96:63]
    node _sumIn_1_0_T_2 = asSInt(_sumIn_1_0_T_1) @[addertree.scala 96:63]
    node sumIn_1_0 = _sumIn_1_0_T_2 @[addertree.scala 49:21 96:31]
    node _GEN_0 = mux(hsk.io_regen_0, sumIn_1_0, r) @[Reg.scala 16:16 17:{18,22}]
    reg r_1 : SInt<9>, clock with :
      reset => (UInt<1>("h0"), r_1) @[Reg.scala 16:16]
    node sumIn_1_1 = asSInt(UInt<9>("h0")) @[addertree.scala 49:21 98:35]
    node _GEN_1 = mux(hsk.io_regen_0, sumIn_1_1, r_1) @[Reg.scala 16:16 17:{18,22}]
    node sumOut_1_0 = r @[addertree.scala 50:22 59:31]
    node sumOut_1_1 = r_1 @[addertree.scala 50:22 59:31]
    io_input_ready <= hsk.io_readyPre @[addertree.scala 41:20]
    io_output_valid <= hsk.io_validNxt @[addertree.scala 43:21]
    io_output_bits_out <= sumOut_1_0 @[addertree.scala 100:24]
    io_output_bits_last <= hsk.io_lastNxt @[addertree.scala 45:25]
    hsk.clock <= clock
    hsk.reset <= reset
    hsk.io_validPre <= io_input_valid @[addertree.scala 40:21]
    hsk.io_lastPre <= io_input_bits_last @[addertree.scala 42:20]
    hsk.io_readyNxt <= io_output_ready @[addertree.scala 44:21]
    hsk.rst <= rst @[addertree.scala 46:13]
    r <= _GEN_0
    r_1 <= _GEN_1
