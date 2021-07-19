library IEEE;
use IEEE.STD_LOGIC_1164.all;

package TIS is
	subtype data_t is std_logic_vector(10 downto 0);
	subtype integer_t is integer range -999 to 999;
	subtype instruction_t is std_logic_vector(17 downto 0);
	subtype address_t is integer range 0 to 15;
	subtype reg_t is std_logic_vector(2 downto 0);
	subtype cpu_t is std_logic_vector(3 downto 0); --integer range 0 to 11;
	subtype nodetap_cmd_t is std_logic_vector(2 downto 0);
	
	type rom_t is array(0 to 15) of instruction_t;
	
	constant WEST: reg_t := "100";
	constant EAST: reg_t := "101";
	constant NORTH: reg_t := "110";
	constant SOUTH: reg_t := "111";
	
	constant NOP: instruction_t := "0101"&"111"&"00001001000";
	constant Z_INSTR: instruction_t := "ZZZZ"&"ZZZ"&"ZZZZZZZZZZZ";
	
	constant Z_DATA: data_t := "ZZZZZZZZZZZ";
	
	constant NODETAP_READ_INSTR: nodetap_cmd_t := "001";
	constant NODETAP_WRITE_INSTR: nodetap_cmd_t := "010";
	constant NODETAP_GET_STATE: nodetap_cmd_t := "011";
	constant NODETAP_READ_IP: nodetap_cmd_t := "100";
	constant NODETAP_WRITE_IP: nodetap_cmd_t := "101";
	constant NODETAP_READ_ACC: nodetap_cmd_t := "110";
	constant NODETAP_WRITE_ACC: nodetap_cmd_t := "111";
end package;
