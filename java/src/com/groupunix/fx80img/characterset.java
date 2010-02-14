package com.groupunix.fx80img;

public class characterset 
{

	private character[] characters = new character[256];
	

	public void setCharacter(int charnum, int[] bits, int len)
	{
		characters[charnum] = new character(bits, len);
		//System.out.println("set character " + charnum + ", proplen = " + len);
		
	}
	
	public int getCharacterCol(int charnum, int colnum)
	{
		return(characters[charnum].getCol(colnum));
	}
}
