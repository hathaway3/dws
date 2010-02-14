package com.groupunix.fx80img;

public class character 
{

	private int[] bits;
	private int len;
	
	public character(int[] bits, int len)
	{
		this.bits = bits;
		this.len = len;
	}
	
	public int getCol(int col)
	{
		// get bits for this col 
		
		return(bits[col]);
	}
	
}
