library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity spi_slave is
	port (
		clk: in std_logic;
		rx_data: out std_logic_vector(7 downto 0);
		tx_data: in std_logic_vector(7 downto 0);
		
		ss: in std_logic; -- Slave Select
		sck: in std_logic; -- Slave Clock
		mosi: in std_logic; -- Master Out / Slave In
		miso: out std_logic -- Master In / Slave Out
	);
end;

architecture Behavioral of spi_slave is
	signal bitmask: std_logic_vector(7 downto 0) := "10000000";
	signal input: std_logic_vector(7 downto 0) := "--------";
	signal sck_prev: std_logic;
	
	function has_bit_set (
		input: std_logic_vector(7 downto 0);
		mask: std_logic_vector(7 downto 0))
		return std_logic is
	begin
		case mask is
			when "00000001" => return input(0);
			when "00000010" => return input(1);
			when "00000100" => return input(2);
			when "00001000" => return input(3);
			when "00010000" => return input(4);
			when "00100000" => return input(5);
			when "01000000" => return input(6);
			when "10000000" => return input(7);
			when others => return '0';
		end case;
	end;
	
begin

	process (clk)
	begin
		if rising_edge(clk) then
			if ss = '1' then
				bitmask <= "10000000";
				miso <= '1';
				sck_prev <= '1';
			elsif sck_prev = '0' and sck = '1' then
				bitmask <= bitmask(0) & bitmask(7 downto 1);
				sck_prev <= '1';
				input <= input(6 downto 0) & mosi;
				miso <= has_bit_set(tx_data, bitmask);
			elsif sck_prev = '1' and sck = '0' then
				sck_prev <= '0';
				miso <= has_bit_set(tx_data, bitmask);
			end if;
		end if;
	end process;
	
	rx_data <= input;

end;

