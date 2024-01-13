`timescale 1ns/1ps

module softmax_tb();
    reg clock;
    reg reset;
    wire io_input_ready;
    reg io_input_valid;
    reg [7:0] io_input_bits_in [15:0];
    reg io_input_bits_last;
    wire io_output_ready=1'b1;
    wire io_output_valid;
    wire [7:0] io_output_bits_out [15:0];
    wire io_output_bits_last;
    reg rst;

    Softmax uut(
        .clock(clock),
        .reset(reset),
        .io_input_ready(io_input_ready),
        .io_input_valid(io_input_valid),
        .io_input_bits_in_0(io_input_bits_in[0]),
        .io_input_bits_in_1(io_input_bits_in[1]),
        .io_input_bits_in_2(io_input_bits_in[2]),
        .io_input_bits_in_3(io_input_bits_in[3]),
        .io_input_bits_in_4(io_input_bits_in[4]),
        .io_input_bits_in_5(io_input_bits_in[5]),
        .io_input_bits_in_6(io_input_bits_in[6]),
        .io_input_bits_in_7(io_input_bits_in[7]),
        .io_input_bits_in_8(io_input_bits_in[8]),
        .io_input_bits_in_9(io_input_bits_in[9]),
        .io_input_bits_in_10(io_input_bits_in[10]),
        .io_input_bits_in_11(io_input_bits_in[11]),
        .io_input_bits_in_12(io_input_bits_in[12]),
        .io_input_bits_in_13(io_input_bits_in[13]),
        .io_input_bits_in_14(io_input_bits_in[14]),
        .io_input_bits_in_15(io_input_bits_in[15]),
        .io_input_bits_last(io_input_bits_last),
        .io_output_ready(io_output_ready),
        .io_output_valid(io_output_valid),
        .io_output_bits_out_0(io_output_bits_out[0]),
        .io_output_bits_out_1(io_output_bits_out[1]),
        .io_output_bits_out_2(io_output_bits_out[2]),
        .io_output_bits_out_3(io_output_bits_out[3]),
        .io_output_bits_out_4(io_output_bits_out[4]),
        .io_output_bits_out_5(io_output_bits_out[5]),
        .io_output_bits_out_6(io_output_bits_out[6]),
        .io_output_bits_out_7(io_output_bits_out[7]),
        .io_output_bits_out_8(io_output_bits_out[8]),
        .io_output_bits_out_9(io_output_bits_out[9]),
        .io_output_bits_out_10(io_output_bits_out[10]),
        .io_output_bits_out_11(io_output_bits_out[11]),
        .io_output_bits_out_12(io_output_bits_out[12]),
        .io_output_bits_out_13(io_output_bits_out[13]),
        .io_output_bits_out_14(io_output_bits_out[14]),
        .io_output_bits_out_15(io_output_bits_out[15]),
        .io_output_bits_last(io_output_bits_last),
        .rst(rst)
    );

    initial begin
        clock=1'b1;
        reset=1'b0;
        rst=1'b0;
        io_input_valid=1'b0;
        io_input_bits_last=1'b0;
        io_input_bits_in[0]=8'h00;
        io_input_bits_in[1]=8'h00;
        io_input_bits_in[2]=8'h00;
        io_input_bits_in[3]=8'h00;
        io_input_bits_in[4]=8'h10;
        io_input_bits_in[5]=8'h10;
        io_input_bits_in[6]=8'h10;
        io_input_bits_in[7]=8'h10;
        io_input_bits_in[8]=8'h20;
        io_input_bits_in[9]=8'h20;
        io_input_bits_in[10]=8'h20;
        io_input_bits_in[11]=8'h20;
        io_input_bits_in[12]=8'h30;
        io_input_bits_in[13]=8'h30;
        io_input_bits_in[14]=8'h30;
        io_input_bits_in[15]=8'h30;
        #3
        reset=1'b1;
        rst=1'b1;
        #100
        reset=1'b0;
        rst=1'b0;
        #10
        io_input_valid=1'b1;
        io_input_bits_last=1'b1;
        #10
        io_input_valid=1'b0;
        io_input_bits_last=1'b0;
    end

    always #5 clock=~clock;
endmodule
