package com.icbc.emall.cas.util;

public class SHA1
{

	public SHA1()
	{
		state = new long[5];
		buffer = new int[64];
		count = 0L;
		state[0] = 0x67452301L;
		state[1] = 0xefcdab89L;
		state[2] = 0x98badcfeL;
		state[3] = 0x10325476L;
		state[4] = 0xc3d2e1f0L;
		SHA1_K0 = 0x5a827999L;
		SHA1_K1 = 0x6ed9eba1L;
		SHA1_K2 = 0x8f1bbcdcL;
		SHA1_K3 = 0xca62c1d6L;
	}

	public int bytetoint(byte data)
	{
		int res = data;
		if(res < 0)
			res += 256;
		return res;
	}

	public byte[] end()
	{
		byte digest[] = new byte[20];
		int counts[] = new int[8];
		int n = (int)(count & 63L);
		count = count << 3;
		for(int i = 7; i >= 0; i--)
		{
			counts[i] = (int)(count & 255L);
			count = count >>> 8;
		}

		if(n >= 56)
		{
			buffer[n] = 128;
			for(int i = ++n; i < 64; i++)
				buffer[i] = 0;

			transform();
			for(int i = 0; i < 56; i++)
				buffer[i] = 0;

			for(int i = 0; i < 8; i++)
				buffer[i + 56] = counts[i];

			transform();
		}
		else
		{
			buffer[n] = 128;
			for(int i = ++n; i < 56; i++)
				buffer[i] = 0;

			for(int i = 0; i < 8; i++)
				buffer[i + 56] = counts[i];

			transform();
		}
		for(int i = 0; i < 5; i++)
		{
			long l = state[i];
			for(int j = 3; j >= 0; j--)
			{
				int m = (int)(l & 255L);
				digest[i * 4 + j] = inttobyte(m);
				l >>>= 8;
			}

			state[i] = 0L;
		}

		return digest;
	}

	public void init()
	{
		count = 0L;
		state[0] = 0x67452301L;
		state[1] = 0xefcdab89L;
		state[2] = 0x98badcfeL;
		state[3] = 0x10325476L;
		state[4] = 0xc3d2e1f0L;
		SHA1_K0 = 0x5a827999L;
		SHA1_K1 = 0x6ed9eba1L;
		SHA1_K2 = 0x8f1bbcdcL;
		SHA1_K3 = 0xca62c1d6L;
	}

	public byte inttobyte(int data)
	{
		byte res;
		if(data >= 128)
			res = (byte)(data - 256);
		else
			res = (byte)data;
		return res;
	}

	public long ROTL(long data, int n)
	{
		long res = data << n | data >>> 32 - n;
		res &= 0xffffffffL;
		return res;
	}

	public void transform()
	{
		long w[] = new long[80];
		for(int i = 0; i < 16; i++)
			w[i] = 0L;

		int j = 0;
		for(int i = 0; i < 64; i++)
		{
			w[j] = w[j] << 8 | (long)(buffer[i] & 0xff);
			if(i % 4 == 3)
				j++;
		}
	//for(int i=0;i<80;i++)
	//	System.out.print("("+i+")"+w[i]+":");
	//System.out.println(":");	
		for(int i = 16; i < 80; i++)
		{
			long temp = w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16];
			//System.out.println(temp);
			w[i] = ROTL(temp, 1);
		}
	//for(int i=0;i<64;i++)
		//System.out.print("("+i+")"+buffer[i]+":");
	//System.out.println(":");
	
	//for(int i=0;i<80;i++)
		//System.out.print("("+i+")"+w[i]+":");
	//System.out.println(":");

		long a = state[0];
		long b = state[1];
		long c = state[2];
		long d = state[3];
		long e = state[4];
		//System.out.println("0->"+state[0]+":"+state[1]+":"+state[2]+":"+state[3]+":"+state[4]);
		for(int i = 0; i < 20; i++)
		{
			long temp = ROTL(a, 5);
			temp = temp + (b & c | ~b & d) + e + w[i] + SHA1_K0;
			temp &= 0xffffffffL;
			e = d;
			d = c;
			c = ROTL(b, 30);
			b = a;
			a = temp;
			//System.out.println("0->"+a+":"+b+":"+c+":"+d+":"+e);
		}
	//System.out.println("1->"+a+":"+b+":"+c+":"+d+":"+e);
		for(int i = 20; i < 40; i++)
		{
			long temp = ROTL(a, 5);
			temp = temp + (b ^ c ^ d) + e + w[i] + SHA1_K1;
			temp &= 0xffffffffL;
			e = d;
			d = c;
			c = ROTL(b, 30);
			b = a;
			a = temp;
		}
	//System.out.println("2->"+a+":"+b+":"+c+":"+d+":"+e);
		for(int i = 40; i < 60; i++)
		{
			long temp = ROTL(a, 5);
			temp = temp + (b & c | c & d | d & b) + e + w[i] + SHA1_K2;
			temp &= 0xffffffffL;
			e = d;
			d = c;
			c = ROTL(b, 30);
			b = a;
			a = temp;
		}
	//System.out.println("3->"+a+":"+b+":"+c+":"+d+":"+e);
		for(int i = 60; i < 80; i++)
		{
			long temp = ROTL(a, 5);
			temp = temp + (b ^ c ^ d) + e + w[i] + SHA1_K3;
			temp &= 0xffffffffL;
			e = d;
			d = c;
			c = ROTL(b, 30);
			b = a;
			a = temp;
		}
	//System.out.println(a+":"+b+":"+c+":"+d+":"+e);
		state[0] = state[0] + a & 0xffffffffL;
		state[1] = state[1] + b & 0xffffffffL;
		state[2] = state[2] + c & 0xffffffffL;
		state[3] = state[3] + d & 0xffffffffL;
		state[4] = state[4] + e & 0xffffffffL;
		for(int i = 0; i < 80; i++)
			w[i] = 0L;
	//System.out.println(state[0]+":"+state[1]+":"+state[2]+":"+state[3]+":"+state[4]);
	}

	public void Update(byte data[], long length)
	{
		int n = (int)(count & 63L);
		count = count + length;
		int l = 64 - n;
		int pos = 0;
		for(; (long)l <= length; l += 64)
		{
			for(int k = n; k < 64; k++)
			{
				buffer[k] = bytetoint(data[pos]);
				pos++;
			}
	    transform();
			n = 0;
		}

		n = (int)(count & 63L);
		for(int k = 0; k < n; k++)
		{
			buffer[k] = bytetoint(data[pos]);
			pos++;
		}

	}

	long count;
	long state[];
	int buffer[];
	long SHA1_K0;
	long SHA1_K1;
	long SHA1_K2;
	long SHA1_K3;
}
