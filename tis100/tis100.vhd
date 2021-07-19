library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use ieee.numeric_std.all;
use work.tis.all;

--
--     00
--     ||
--     01
--     ||
--    UART
--

entity tis100 is
	port (
		clk_50mhz: in std_logic;
		
		ss: in std_logic;
		sck: in std_logic;
		mosi: in std_logic;
		miso: out std_logic;
		
		ana_ss: out std_logic;
		ana_sck: out std_logic;
		ana_mosi: out std_logic;
		ana_miso: out std_logic
	);
end tis100;

architecture Behavioral of tis100 is
	component TisTap
		port (
			reset_in: in std_logic;
			clk_in: in std_logic;
			
			stdout_data: in data_t;
			stdout_send: out std_logic;
			stdout_recv: in std_logic;
			
			nodetap_cpu: out cpu_t;
			nodetap_cmd: out nodetap_cmd_t;
			nodetap_data: inout instruction_t;
			nodetap_pause: out std_logic;
			
			ss: in std_logic;
			sck: in std_logic;
			mosi: in std_logic;
			miso: out std_logic;
			
			reset_out: out std_logic;
			clk_out: out std_logic
		);
	end component;

	component TisRow
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
	end component;
	
	signal stdout_data: data_t;
	signal stdout_send: std_logic;
	signal stdout_recv: std_logic;
	
	signal system_reset: std_logic;
	
	signal node_reset: std_logic;
	signal node_clk: std_logic;
	
	--  00 <=> 10 <=> 20 <=> 30
	--  /\     /\     /\     /\
	--  ||     ||     ||     ||
	--  \/     \/     \/     \/
	--  01 <=> 11 <=> 21 <=> 31
	--  /\     /\     /\     /\
	--  ||     ||     ||     ||
	--  \/     \/     \/     \/
	--  02 <=> 12 <=> 22 <=> 32
	
	signal nodetap_cpu: cpu_t;
	signal nodetap_cmd: nodetap_cmd_t;
	signal nodetap_data: instruction_t;
	signal nodetap_pause: std_logic;
	
	signal row0_1_data: std_logic_vector(43 downto 0);
	signal row1_2_data: std_logic_vector(43 downto 0);
	
	signal row0_to_1: std_logic_vector(3 downto 0);
	signal row1_to_0: std_logic_vector(3 downto 0);
	
	signal row1_to_2: std_logic_vector(3 downto 0);
	signal row2_to_1: std_logic_vector(3 downto 0);
	
--	constant node00_rom: rom_t := (
--		0 => "0101"&"111"&"00001001000", -- MOV 'H', DOWN
--		1 => "0101"&"111"&"00001100101", -- MOV 'e', DOWN
--		2 => "0101"&"111"&"00001101100", -- MOV 'l', DOWN
--		3 => "0101"&"111"&"00001101100", -- MOV 'l', DOWN
--		4 => "0101"&"111"&"00001101111", -- MOV 'o', DOWN
--		5 => "0101"&"111"&"00000001101", -- MOV '\r', DOWN
--		6 => "0101"&"111"&"00000001010", -- MOV '\n', DOWN
--		7 => "1011"&"000"&"00000000000",  -- JMP 0
--		others => NOP
--	);
--	
--	constant node01_rom: rom_t := (
--		0 => "0100"&"111"&"110"&"00000000", -- MOV UP, DOWN
--		1 => "1011"&"000"&"00000000000",  -- JMP 0
--		others => NOP
--	);
	
	signal clk_div: unsigned(12 downto 0);
	signal miso_tmp: std_logic;
begin
	system_reset <= '0';
	
	process (clk_50mhz)
	begin
		if rising_edge(clk_50mhz) then
			clk_div <= clk_div + 1;
		end if;
	end process;
	
	TISTAP0: TisTap port map(
		reset_in => system_reset,
		reset_out => node_reset,
		
		clk_in => clk_div(9),
		clk_out => node_clk,

		ss => ss,
		sck => sck,
		miso => miso_tmp,
		mosi => mosi,
		
		stdout_data => stdout_data,
		stdout_send => stdout_recv,
		stdout_recv => stdout_send,
		
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_cpu => nodetap_cpu,
		nodetap_pause => nodetap_pause
	);
	
	TisRow0: TisRow 
	generic map(
		y => "00"
	)
	port map(
		clk => node_clk,
		reset => node_reset,
		
		nodetap_cpu => nodetap_cpu,
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_pause => nodetap_pause,
			
		-- North
		n_io_send => open,
		n_io_recv => (others => '0'),
		n_io_data => open,
		
		-- South
		s_io_send => row0_to_1,
		s_io_recv => row1_to_0,
		s_io_data => row0_1_data
	);
	
	TisRow1: TisRow 
	generic map(
		y => "01"
	)
	port map(
		clk => node_clk,
		reset => node_reset,
		
		nodetap_cpu => nodetap_cpu,
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_pause => nodetap_pause,
			
		-- North
		n_io_send => row1_to_0,
		n_io_recv => row0_to_1,
		n_io_data => row0_1_data,
		
		-- South
		s_io_send => row1_to_2,
		s_io_recv => row2_to_1,
		s_io_data => row1_2_data
	);
	
	TisRow2: TisRow 
	generic map(
		y => "10"
	)
	port map(
		clk => node_clk,
		reset => node_reset,
		
		nodetap_cpu => nodetap_cpu,
		nodetap_cmd => nodetap_cmd,
		nodetap_data => nodetap_data,
		nodetap_pause => nodetap_pause,
			
		-- North
		n_io_send => row2_to_1,
		n_io_recv => row1_to_2,
		n_io_data => row1_2_data,
		
		-- South
		s_io_send => open,
		s_io_recv => (others => '0'),
		s_io_data => open
	);
	
	ana_ss <= ss;
	ana_sck <= sck;
	ana_miso <= miso_tmp;
	miso <= miso_tmp;
	ana_mosi <= mosi;
	
end Behavioral;

