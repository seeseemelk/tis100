library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use ieee.numeric_std.all;
use work.TIS.all;

entity T21 is
	generic (
		rom: rom_t;
		cpu_id: cpu_t
	);
	port (
		reset: in std_logic;
		clk: in std_logic;
		
		-- Debug
		nodetap_cpu: in cpu_t;
		nodetap_cmd: in nodetap_cmd_t;
		nodetap_data: inout instruction_t := Z_INSTR;
		nodetap_pause: in std_logic;
		
		-- Northern I/O
		n_io_data: inout data_t := Z_DATA;
		n_io_send: out std_logic;
		n_io_recv: in std_logic;
		
		-- Eastern I/O
		e_io_data: inout data_t := Z_DATA;
		e_io_send: out std_logic;
		e_io_recv: in std_logic;
		
		-- Southern I/O
		s_io_data: inout data_t := Z_DATA;
		s_io_send: out std_logic;
		s_io_recv: in std_logic;
		
		-- Western I/O
		w_io_data: inout data_t := Z_DATA;
		w_io_send: out std_logic;
		w_io_recv: in std_logic
	);
end T21;


architecture Behavioral of T21 is
	subtype register_type is std_logic_vector(2 downto 0);
	
	type state is (
		FETCH,
		FETCH_WAIT,
		FETCH_END,
		EXECUTE,
		COMMIT,
		COMMIT_WAIT,
		COMMIT_END,
		FINISH,
		FATAL_FETCH,
		FATAL_FETCH_WAIT,
		FATAL_FETCH_END,
		FATAL
	);
		
	signal reg_ip: address_t := 0;
	signal reg_acc: integer_t := 0;
	signal ram: rom_t := rom;
	
	function state_to_vector (s: state)
		return std_logic_vector is
	begin
		case s is
			when FETCH            => return "0000";
			when FETCH_WAIT       => return "0001";
			when FETCH_END        => return "0010";
			when EXECUTE          => return "0011";
			when COMMIT           => return "0100";
			when COMMIT_WAIT      => return "0101";
			when COMMIT_END       => return "0110";
			when FINISH           => return "0111";
			when FATAL_FETCH      => return "1000";
			when FATAL_FETCH_WAIT => return "1001";
			when FATAL_FETCH_END  => return "1010";
			when FATAL            => return "1111";
		end case;
	end function;
begin

	process (reset, clk) is
		variable current_instruction: instruction_t;
		variable opcode: std_logic_vector(3 downto 0);
		variable lit: data_t;
		variable lit_val: integer_t;
		variable lit_addr: address_t;
		
		variable dest_reg: register_type;
		variable src_reg: register_type;
		
		variable cpu_state: state := FETCH;
		variable reg_tmp: integer_t := 0;
	begin
	
		if reset = '1' then
			reg_ip <= 0;
			reg_acc <= 0;
			reg_tmp := 0;
			cpu_state := FETCH;
			s_io_data <= Z_DATA;
			n_io_data <= Z_DATA;
		elsif rising_edge(clk) then
			
			if nodetap_cpu = cpu_id then
				case nodetap_cmd is
					when NODETAP_READ_INSTR =>
						nodetap_data <= ram(reg_ip);
					when NODETAP_WRITE_INSTR =>
						ram(reg_ip) <= nodetap_data;
					when NODETAP_READ_IP =>
						nodetap_data <= std_logic_vector(to_unsigned(reg_ip, instruction_t'length));
					when NODETAP_WRITE_IP =>
						reg_ip <= to_integer(unsigned(nodetap_data(3 downto 0)));
					when NODETAP_READ_ACC =>
						nodetap_data <= std_logic_vector(to_unsigned(reg_acc, instruction_t'length));
					when NODETAP_WRITE_ACC =>
						reg_acc <= to_integer(unsigned(nodetap_data(11 downto 0)));
					when NODETAP_GET_STATE =>
						nodetap_data <= "00000000000000" & state_to_vector(cpu_state);
					when others =>
						nodetap_data <= Z_INSTR;
				end case;
			else
				nodetap_data <= Z_INSTR;
			end if;
			
			if nodetap_pause = '0' then
				if cpu_state = FETCH then
					current_instruction := ram(reg_ip);
					opcode := current_instruction(17 downto 14);
					dest_reg := current_instruction(13 downto 11);
					src_reg := current_instruction(10 downto 8);
					lit := current_instruction(10 downto 0);
					lit_val := to_integer(unsigned(lit));
					lit_addr := to_integer(unsigned(lit));
					
					if opcode(3) = '1' then
						reg_tmp := lit_addr;
						cpu_state := EXECUTE;
					elsif opcode(0) = '1' then
						reg_tmp := lit_val;
						cpu_state := EXECUTE;
					else
						case src_reg is
							when "000" =>
								reg_tmp := 0;
								cpu_state := EXECUTE;
							when "001" =>
								reg_tmp := reg_acc;
								cpu_state := EXECUTE;
							when NORTH | EAST | SOUTH | WEST =>
								cpu_state := FETCH_WAIT;
							when others =>
								cpu_state := FATAL_FETCH;
						end case;
					end if;
				end if;
				
				if cpu_state = FETCH_WAIT then
					case src_reg is
						when NORTH =>
							if n_io_recv = '1' then
								n_io_send <= '1';
								reg_tmp := to_integer(unsigned(n_io_data));
								cpu_state := FETCH_END;
							end if;
						when EAST =>
							if e_io_recv = '1' then
								e_io_send <= '1';
								reg_tmp := to_integer(unsigned(e_io_data));
								cpu_state := FETCH_END;
							end if;
						when SOUTH =>
							if s_io_recv = '1' then
								s_io_send <= '1';
								reg_tmp := to_integer(unsigned(s_io_data));
								cpu_state := FETCH_END;
							end if;
						when WEST =>
							if w_io_recv = '1' then
								w_io_send <= '1';
								reg_tmp := to_integer(unsigned(w_io_data));
								cpu_state := FETCH_END;
							end if;
						when others =>
							cpu_state := FATAL_FETCH_WAIT;
					end case;
				elsif cpu_state = FETCH_END then
					case src_reg is
						when NORTH =>
							if n_io_recv = '0' then
								n_io_send <= '0';
								cpu_state := EXECUTE;
							end if;
						when others =>
							cpu_state := FATAL_FETCH_END;
					end case;
				end if;
				
				if cpu_state = EXECUTE then
					case opcode is
						when "0000" | "0001" =>
							-- ADD <SRC/LIT>
							reg_tmp := reg_acc + reg_tmp;
							cpu_state := COMMIT;
						when "0010" | "0011" =>
							-- SUB <SRC/LIT>
							reg_tmp := reg_acc - reg_tmp;
							cpu_state := COMMIT;
						when "0100" | "0101" =>
							-- MOV <SRC/LIT> <DST>
							cpu_state := COMMIT;
						when "1011" =>
							-- JMP <ADDR>
							reg_ip <= reg_tmp;
							cpu_state := FETCH;
						when "1100" =>
							-- JEZ <ADDR>
							if reg_acc = 0 then
								reg_ip <= reg_tmp;
								cpu_state := FETCH;
							else
								cpu_state := FINISH;
							end if;
						when "1101" =>
							-- JNZ <ADDR>
							if reg_acc /= 0 then
								reg_ip <= reg_tmp;
								cpu_state := FETCH;
							else
								cpu_state := FINISH;
							end if;
						when "1110" =>
							-- JGZ <ADDR>
							if reg_acc > 0 then
								reg_ip <= reg_tmp;
								cpu_state := FETCH;
							else
								cpu_state := FINISH;
							end if;
						when "1111" =>
							-- JLZ <ADDR>
							if reg_acc < 0 then
								reg_ip <= reg_tmp;
								cpu_state := FETCH;
							else
								cpu_state := FINISH;
							end if;
						when others =>
							-- Unsupported
							cpu_state := FATAL;
					end case;
				end if;
				
				if cpu_state = COMMIT then
					case dest_reg is
						when "000" =>
							-- NIL
							cpu_state := FATAL;
						when "001" =>
							-- ACC
							reg_acc <= reg_tmp;
							cpu_state := FINISH;
						when "010" =>
							-- ANY
							-- TODO
							cpu_state := FATAL;
						when "011" =>
							-- LAST
							-- TODO
							cpu_state := FATAL;
						when NORTH =>
							n_io_send <= '1';
							n_io_data <= std_logic_vector(to_unsigned(reg_tmp, 11));
							cpu_state := COMMIT_WAIT;
						when EAST =>
							e_io_send <= '1';
							e_io_data <= std_logic_vector(to_unsigned(reg_tmp, 11));
							cpu_state := COMMIT_WAIT;
						when SOUTH =>
							s_io_send <= '1';
							s_io_data <= std_logic_vector(to_unsigned(reg_tmp, 11));
							cpu_state := COMMIT_WAIT;
						when WEST =>
							w_io_send <= '1';
							w_io_data <= std_logic_vector(to_unsigned(reg_tmp, 11));
							cpu_state := COMMIT_WAIT;
						when others =>
							-- ???
							cpu_state := FATAL;
					end case;
				elsif cpu_state = COMMIT_WAIT then
					case dest_reg is
						when NORTH =>
							if n_io_recv = '1' then
								n_io_send <= '0';
								n_io_data <= Z_DATA;
								cpu_state := COMMIT_END;
							end if;
						when EAST =>
							if e_io_recv = '1' then
								e_io_send <= '0';
								e_io_data <= Z_DATA;
								cpu_state := COMMIT_END;
							end if;
						when SOUTH =>
							if s_io_recv = '1' then
								s_io_send <= '0';
								s_io_data <= Z_DATA;
								cpu_state := COMMIT_END;
							end if;
						when WEST =>
							if w_io_recv = '1' then
								w_io_send <= '0';
								w_io_data <= Z_DATA;
								cpu_state := COMMIT_END;
							end if;
						when others =>
							cpu_state := FATAL;
					end case;
				elsif cpu_state = COMMIT_END then
					case dest_reg is
						when NORTH =>
							if n_io_recv = '0' then
								cpu_state := FINISH;
							end if;
						when EAST =>
							if e_io_recv = '0' then
								cpu_state := FINISH;
							end if;
						when SOUTH =>
							if s_io_recv = '0' then
								cpu_state := FINISH;
							end if;
						when WEST =>
							if w_io_recv = '0' then
								cpu_state := FINISH;
							end if;
						when others =>
							cpu_state := FATAL;
					end case;
				end if;
				
				if cpu_state = FINISH then
					-- Increment instruction pointer
					if reg_ip = address_t'high then
						reg_ip <= 0;
					else
						reg_ip <= reg_ip + 1;
					end if;
					
					cpu_state := FETCH;
				end if;
			end if; -- nodetap_pause
		end if; -- clk
	end process;

end Behavioral;

