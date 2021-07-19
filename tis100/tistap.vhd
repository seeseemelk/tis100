library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

use work.tis.all;

entity TisTap is
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
		
		reset_out: out std_logic;
		clk_out: out std_logic;
		
		ss: in std_logic;
		sck: in std_logic;
		mosi: in std_logic;
		miso: out std_logic
	);
end TisTap;

architecture Behavioral of TisTap is
	component spi_slave is
		port (
			clk: in std_logic;
			rx_data: out std_logic_vector(7 downto 0);
			tx_data: in std_logic_vector(7 downto 0);
			
			ss: in std_logic; -- Slave Select
			sck: in std_logic; -- Slave Clock
			mosi: in std_logic; -- Master Out / Slave In
			miso: out std_logic -- Master In / Slave Out
		);
	end component;
	
	type state_t is (
		NODETAP_TRANSFER_0,
		NODETAP_TRANSFER_1,
		NODETAP_TRANSFER_2
	);
	
	signal state: state_t := NODETAP_TRANSFER_0;
	signal stdout_send_internal: std_logic := '0';
	signal spi_rx: std_logic_vector(7 downto 0);
	signal spi_tx: std_logic_vector(7 downto 0) := "00000000";
	signal running: std_logic := '1';
	signal ss_prev: std_logic;
	signal has_stdout: std_logic := '0';
	signal step: std_logic := '0';
begin

	SPI0 : spi_slave port map (
		clk => clk_in,
		ss => ss,
		sck => sck,
		miso => miso,
		mosi => mosi,
		
		rx_data => spi_rx,
		tx_data => spi_tx
	);
	
	process (reset_in, clk_in) is
		variable has_stdout: std_logic;
	begin
		if reset_in = '1' then
			has_stdout := '0';
			running <= '1';
			ss_prev <= '0';
		elsif clk_in'event and clk_in = '1' then
			has_stdout := '1' when stdout_recv = '1' and stdout_send_internal = '0' else
			              '0';
			if stdout_recv = '0' then
				stdout_send_internal <= '0';
			end if;
			
			if step = '1' then
				running <= '0';
				step <= '0';
			end if;
		
			if ss = '1' and ss_prev = '0' then
			
				if spi_rx = "00000000" then
					-- No operation
					null;
				elsif spi_rx = "00000001" then
					-- Read Status
					spi_tx <= "000000" & has_stdout & running;
				elsif spi_rx = "00000010" then
					-- Pause
					running <= '0';
					spi_tx <= (others => '0');
				elsif spi_rx = "00000011" then
					-- Continue
					running <= '1';
					spi_tx <= (others => '0');
				elsif spi_rx = "00000100" then
					-- Read stdout
					if stdout_recv = '1' then
						stdout_send_internal <= '1';
						spi_tx <= stdout_data(7 downto 0);
					else
						spi_tx <= (others => '1');
					end if;
				elsif spi_rx(7 downto 4) = "0001" then
					-- Nodetap send
					nodetap_cpu <= spi_rx(3 downto 0);
					state <= NODETAP_TRANSFER_0;
				elsif spi_rx(7 downto 4) = "0010" then
					-- Nodetap set command
					nodetap_cmd <= spi_rx(2 downto 0);
					nodetap_data <= Z_INSTR;
				elsif spi_rx = "00110000" then
					-- Nodetap read data
					if state = NODETAP_TRANSFER_0 then
						spi_tx <= nodetap_data(7 downto 0);
						state <= NODETAP_TRANSFER_1;
					elsif state = NODETAP_TRANSFER_1 then
						spi_tx <= nodetap_data(15 downto 8);
						state <= NODETAP_TRANSFER_2;
					elsif state = NODETAP_TRANSFER_2 then
						spi_tx <= "000000" & nodetap_data(17 downto 16);
						state <= NODETAP_TRANSFER_0;
					end if;
				elsif spi_rx(7 downto 4) = "0100" then
					-- Nodetap write least significant
					if state = NODETAP_TRANSFER_0 then
						nodetap_data(3 downto 0) <= spi_rx(3 downto 0);
						state <= NODETAP_TRANSFER_1;
					elsif state = NODETAP_TRANSFER_1 then
						nodetap_data(7 downto 4) <= spi_rx(3 downto 0);
						state <= NODETAP_TRANSFER_2;
					elsif state = NODETAP_TRANSFER_2 then
						nodetap_data(11 downto 8) <= spi_rx(3 downto 0);
						state <= NODETAP_TRANSFER_0;
					end if;
				elsif spi_rx(7 downto 4) = "0101" then
					-- Nodetap write most significant
					if state = NODETAP_TRANSFER_0 then
						nodetap_data(15 downto 12) <= spi_rx(3 downto 0);
						state <= NODETAP_TRANSFER_1;
					elsif state = NODETAP_TRANSFER_1 then
						nodetap_data(17 downto 16) <= spi_rx(1 downto 0);
						state <= NODETAP_TRANSFER_0;
					else
						state <= NODETAP_TRANSFER_0;
					end if;
				elsif spi_rx = "01100000" then
					-- Step
					step <= '1';
					running <= '1';
				elsif spi_rx = "10101010" then
					-- Identify
					spi_tx <= "01010110";
				else
					-- Others
					spi_tx <= "11111111";
				end if;

--				case spi_rx is
--					when "00000000" =>
--						-- No Operation
--						null;
--					when "00000001" =>
--						-- Read Status
--						spi_tx <= "000000" & has_stdout & running;
--					when "00000010" =>
--						-- Pause
--						running <= '0';
--						spi_tx <= (others => '0');
--					when "00000011" =>
--						-- Continue
--						running <= '1';
--						spi_tx <= (others => '0');
--					when "00000100" =>
--						-- Read stdout
--						if stdout_recv = '1' then
--							stdout_send_internal <= '1';
--							spi_tx <= stdout_data(7 downto 0);
--						else
--							spi_tx <= (others => '1');
--						end if;
--					when "0001----" =>
--						-- Nodetap send
--						nodetap_cpu <= spi_rx(3 downto 0);
--						state <= READ_NODETAP_0;
--					when "0010----" =>
--						-- Nodetap set command
--						nodetap_cmd <= spi_rx(3 downto 0);
--					when "00110000" =>
--						-- Nodetap read data
--						if state = READ_NODETAP_0 then
--							spi_tx <= nodetap_data(7 downto 0);
--							state <= READ_NODETAP_1;
--						elsif state = READ_NODETAP_1 then
--							spi_tx <= nodetap_data(15 downto 8);
--							state <= READ_NODETAP_2;
--						elsif state = READ_NODETAP_2 then
--							spi_tx <= "000000" & nodetap_data(17 downto 16);
--							state <= READ_NODETAP_0;
--						end if;
--					when "10101010" =>
--						-- Identify
--						spi_tx <= "01010110";
--					when others =>
--						spi_tx <= "11111010";
--				end case;
			end if;
			ss_prev <= ss;
		end if;
	end process;
	
	reset_out <= reset_in;
	clk_out <= clk_in;
	nodetap_pause <= '0' when running = '1' else
	                 '1';
	stdout_send <= stdout_send_internal;

end Behavioral;

