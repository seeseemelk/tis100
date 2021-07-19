
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity clockDiv is
	port (
		reset: in std_logic;
		clock_in: in std_logic;
		clock_out: out std_logic
	);
end clockDiv;

architecture Behavioral of clockDiv is
	signal counter: natural range 0 to 2603 := 0;
	signal state: std_logic := '0';
begin
	process (clock_in, reset)
	begin
		if reset = '1' then
			counter <= 0;
			state <= '0';
		elsif clock_in'event and clock_in = '1' then
			if counter = 0 then
				state <= not state;
				counter <= 1;
			elsif counter = 2603 then
				counter <= 0;
			else
				counter <= counter + 1;
			end if;
		end if;
	end process;
	
	clock_out <= state;
end Behavioral;

