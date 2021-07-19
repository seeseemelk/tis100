library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;
use work.tis.all;

entity TisRow is
	generic (
		y: in std_logic_vector(1 downto 0)
	);
	port (
		reset: in std_logic;
		clk: in std_logic;
		
		nodetap_cpu: in cpu_t;
		nodetap_cmd: in nodetap_cmd_t;
		nodetap_data: inout instruction_t := Z_INSTR;
		nodetap_pause: in std_logic;
		
		-- North
		n_io_send: out std_logic_vector(3 downto 0);
		n_io_recv: in std_logic_vector(3 downto 0);
		n_io_data: inout std_logic_vector(43 downto 0);
		
		-- South
		s_io_send: out std_logic_vector(3 downto 0);
		s_io_recv: in std_logic_vector(3 downto 0);
		s_io_data: inout std_logic_vector(43 downto 0)
	);
end TisRow;

architecture Behavioral of TisRow is
	component T21
		generic (
			rom: in rom_t;
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
			
			-- North
			n_io_send: out std_logic;
			n_io_recv: in std_logic;
			n_io_data: inout std_logic_vector(10 downto 0);
			
			-- East
 			e_io_send: out std_logic;
			e_io_recv: in std_logic;
			e_io_data: inout std_logic_vector(10 downto 0);
			
			-- South
			s_io_send: out std_logic;
			s_io_recv: in std_logic;
			s_io_data: inout std_logic_vector(10 downto 0);
			
			-- West
			w_io_send: out std_logic;
			w_io_recv: in std_logic;
			w_io_data: inout std_logic_vector(10 downto 0)
		);
	end component;
	
--	signal node0y_n_data: std_logic_vector(10 downto 0);
--	signal node0y_s_data: std_logic_vector(10 downto 0);
--	signal node1y_n_data: std_logic_vector(10 downto 0);
--	signal node1y_s_data: std_logic_vector(10 downto 0);
--	signal node2y_n_data: std_logic_vector(10 downto 0);
--	signal node2y_s_data: std_logic_vector(10 downto 0);
--	
--	signal node0y_n_send: std_logic;
--	signal node0y_n_recv std_logic;
--	signal node1y_n_send: std_logic;
--	signal node1y_n_recv std_logic;
--	signal node2y_n_send: std_logic;
--	signal node2y_n_recv std_logic;
	
	signal node0y_1y_data: std_logic_vector(10 downto 0);
	signal node1y_2y_data: std_logic_vector(10 downto 0);
	signal node2y_3y_data: std_logic_vector(10 downto 0);
	
	signal node0y_to_1y: std_logic;
	signal node1y_to_0y: std_logic;
	
	signal node1y_to_2y: std_logic;
	signal node2y_to_1y: std_logic;
	
	signal node2y_to_3y: std_logic;
	signal node3y_to_2y: std_logic;
begin

	NODE0Y: T21
	generic map(
		rom => (others => NOP),
		cpu_id => "00" & y
	)
	port map(
		reset => reset,
		clk => clk,
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_cpu => nodetap_cpu,
		nodetap_pause => nodetap_pause,
		n_io_send => n_io_send(0),
		n_io_recv => n_io_recv(0),
		n_io_data => n_io_data(10 downto 0),
		s_io_send => s_io_send(0),
		s_io_recv => s_io_recv(0),
		s_io_data => s_io_data(10 downto 0),
		w_io_send => open,
		w_io_recv => '0',
		w_io_data => open,
		e_io_send => node0y_to_1y,
		e_io_recv => node1y_to_0y,
		e_io_data => node0y_1y_data
	);
	
	NODE1Y: T21
	generic map(
		rom => (others => NOP),
		cpu_id => "01" & y
	)
	port map(
		reset => reset,
		clk => clk,
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_cpu => nodetap_cpu,
		nodetap_pause => nodetap_pause,
		n_io_send => n_io_send(1),
		n_io_recv => n_io_recv(1),
		n_io_data => n_io_data(21 downto 11),
		s_io_send => s_io_send(1),
		s_io_recv => s_io_recv(1),
		s_io_data => s_io_data(21 downto 11),
		w_io_send => node1y_to_0y,
		w_io_recv => node0y_to_1y,
		w_io_data => node0y_1y_data,
		e_io_send => node1y_to_2y,
		e_io_recv => node2y_to_1y,
		e_io_data => node1y_2y_data
	);
	
	NODE2Y: T21
	generic map(
		rom => (others => NOP),
		cpu_id => "10" & y
	)
	port map(
		reset => reset,
		clk => clk,
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_cpu => nodetap_cpu,
		nodetap_pause => nodetap_pause,
		n_io_send => n_io_send(2),
		n_io_recv => n_io_recv(2),
		n_io_data => n_io_data(32 downto 22),
		s_io_send => s_io_send(2),
		s_io_recv => s_io_recv(2),
		s_io_data => s_io_data(32 downto 22),
		w_io_send => node2y_to_1y,
		w_io_recv => node1y_to_2y,
		w_io_data => node1y_2y_data,
		e_io_send => node2y_to_3y,
		e_io_recv => node3y_to_2y,
		e_io_data => node2y_3y_data
	);
	
	NODE3Y: T21
	generic map(
		rom => (others => NOP),
		cpu_id => "11" & y
	)
	port map(
		reset => reset,
		clk => clk,
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_cpu => nodetap_cpu,
		nodetap_pause => nodetap_pause,
		n_io_send => n_io_send(3),
		n_io_recv => n_io_recv(3),
		n_io_data => n_io_data(43 downto 33),
		s_io_send => s_io_send(3),
		s_io_recv => s_io_recv(3),
		s_io_data => s_io_data(43 downto 33),
		w_io_send => node3y_to_2y,
		w_io_recv => node2y_to_3y,
		w_io_data => node2y_3y_data,
		e_io_send => open,
		e_io_recv => '0',
		e_io_data => open
	);

end Behavioral;

