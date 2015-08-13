package com.icbc.emall.cas.util;

import java.util.Vector;

public final class Base64
{

	public Base64()
	{
		vBuffer = new Vector(MEMUNIT, MEMUNIT);
		byLeft = new byte[4];
		byLeft[0] = byLeft[1] = byLeft[2] = byLeft[3] = 0;
		nLeft = 0;
		bEndOfDecode = false;
	}

	public void decode(byte byInput[], int nInputLength)
	{
		if(bEndOfDecode)
			return;
		int nNewLength = nInputLength + nLeft;
		byte byNewInput[] = new byte[nNewLength];
		int i;
		for(i = 0; i < nLeft; i++)
			byNewInput[i] = byLeft[i];

		for(int j = 0; j < nInputLength;)
		{
			byNewInput[i] = byInput[j];
			j++;
			i++;
		}

		byte byBuffer[] = new byte[4];
		int nBuffer = 0;
		for(i = 0; i < nNewLength && !bEndOfDecode; i++)
		{
			if(nBuffer < 4)
				if(getReverseMapping(byNewInput[i]) >= 0)
				{
					byBuffer[nBuffer++] = byNewInput[i];
					if(nBuffer < 4)
						continue;
				}
				else
				{
					if(byNewInput[i] != pad)
						continue;
					bEndOfDecode = true;
					if(nBuffer == 0 || nBuffer == 1)
					{
						nBuffer = 0;
						continue;
					}
				}
			int j;
			int n = j = 0;
			for(; j < nBuffer; j++)
				n = (n << 6) + (getReverseMapping(byBuffer[j]) & 0x3f);

			for(; j < 4; j++)
				n <<= 6;

			if(nBuffer == 4)
			{
				vBuffer.addElement(new Byte((byte)(n >> 16 & 0xff)));
				vBuffer.addElement(new Byte((byte)(n >> 8 & 0xff)));
				vBuffer.addElement(new Byte((byte)(n & 0xff)));
			}
			else
			if(nBuffer == 3)
			{
				vBuffer.addElement(new Byte((byte)(n >> 16 & 0xff)));
				vBuffer.addElement(new Byte((byte)(n >> 8 & 0xff)));
			}
			else
			{
				vBuffer.addElement(new Byte((byte)(n >> 16 & 0xff)));
			}
			nBuffer = 0;
		}

		if(!bEndOfDecode)
		{
			for(i = 0; i < nBuffer; i++)
				byLeft[i] = byBuffer[i];

			nLeft = nBuffer;
		}
		else
		{
			nLeft = 0;
		}
	}

	public void encode(byte byInput[], int nInputLength)
	{
		if(nInputLength == 0)
			return;
		int nNewInputLength = nLeft + nInputLength;
		byte byNewInput[] = new byte[nNewInputLength];
		int i;
		for(i = 0; i < nLeft; i++)
			byNewInput[i] = byLeft[i];

		for(int j = 0; j < nInputLength; j++)
		{
			byNewInput[i] = byInput[j];
			i++;
		}

		for(i = 0; i + 2 < nNewInputLength; i += 3)
		{
			int n = ((byNewInput[i] & 0xff) << 16) + ((byNewInput[i + 1] & 0xff) << 8) + (byNewInput[i + 2] & 0xff);
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 18 & 0x3f)]));
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 12 & 0x3f)]));
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 6 & 0x3f)]));
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n & 0x3f)]));
		}

		for(nLeft = 0; i < nNewInputLength; nLeft++)
		{
			byLeft[nLeft] = byNewInput[i];
			i++;
		}

	}

	public void endDecode()
	{
		if(nLeft > 1 && nLeft < 4)
		{
			int j;
			int n = j = 0;
			for(; j < nLeft; j++)
				n = (n << 6) + (getReverseMapping(byLeft[j]) & 0x3f);

			for(; j < 4; j++)
				n <<= 6;

			if(nLeft == 3)
			{
				vBuffer.addElement(new Byte((byte)(n >> 16 & 0xff)));
				vBuffer.addElement(new Byte((byte)(n >> 8 & 0xff)));
			}
			else
			{
				vBuffer.addElement(new Byte((byte)(n >> 16 & 0xff)));
			}
		}
		nLeft = 0;
		bEndOfDecode = true;
	}

	public void endEncode()
	{
		if(nLeft == 1)
		{
			int n = (byLeft[0] & 0xff) << 16;
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 18 & 0x3f)]));
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 12 & 0x3f)]));
			vBuffer.addElement(new Byte(pad));
			vBuffer.addElement(new Byte(pad));
		}
		else
		if(nLeft == 2)
		{
			int n = ((byLeft[0] & 0xff) << 16) + ((byLeft[1] & 0xff) << 8);
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 18 & 0x3f)]));
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 12 & 0x3f)]));
			vBuffer.addElement(new Byte(base64Alphabet[(byte)(n >> 6 & 0x3f)]));
			vBuffer.addElement(new Byte(pad));
		}
	}

	public byte[] getDecodedResult()
	{
		int size = vBuffer.size();
		byte byTemp[] = new byte[size];
		for(int i = 0; i < size; i++)
			byTemp[i] = ((Byte)vBuffer.elementAt(i)).byteValue();

		return byTemp;
	}

	public byte[] getEncodedResult()
	{
		int size = vBuffer.size();
		if(size == 0)
			return new byte[0];
		byte byTemp[] = new byte[size + ((size - 1) / MAXLINELEN) * 2];
		int j;
		for(int i = j = 0; i < size;)
		{
			byTemp[j++] = ((Byte)vBuffer.elementAt(i++)).byteValue();
			if(i % MAXLINELEN == 0 && i < size)
			{
				byTemp[j++] = cr;
				byTemp[j++] = lf;
			}
		}

		return byTemp;
	}

	private byte getReverseMapping(byte by)
	{
		int n = base64Alphabet.length;
		for(int i = 0; i < n; i++)
			if(by == base64Alphabet[i])
				return (byte)i;

		return -1;
	}

	public void startDecode()
	{
		vBuffer.removeAllElements();
		byLeft[0] = byLeft[1] = byLeft[2] = 0;
		nLeft = 0;
		bEndOfDecode = false;
	}

	public void startEncode()
	{
		vBuffer.removeAllElements();
		byLeft[0] = byLeft[1] = byLeft[2] = 0;
		nLeft = 0;
	}

	private static int MEMUNIT = 1024;
	private static int MAXLINELEN = 76;
	private static byte base64Alphabet[] = {
		65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 
		75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 
		85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 
		101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 
		111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 
		121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 
		56, 57, 43, 47
	};
	private static byte pad = 61;
	private static byte cr = 13;
	private static byte lf = 10;
	private Vector vBuffer;
	private byte byLeft[];
	private int nLeft;
	private boolean bEndOfDecode;

}
