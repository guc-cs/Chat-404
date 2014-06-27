package engine;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionLayer {

	private Inflater inflater;
	private Deflater deflater;
	private byte[] outBuff;

	
	public CompressionLayer() {

		inflater = new Inflater();
		deflater = new Deflater();

	}

	public byte [] compress(byte [] msg)
	{			
		byte [] temp = new byte[msg.length];
		deflater.setInput(msg);
		deflater.finish();
		int cSize = deflater.deflate(temp);
		deflater.reset();
		ByteBuffer outBuffer = ByteBuffer.wrap(temp);
		outBuff = new byte [cSize];
		outBuffer.get(outBuff);
		return outBuff;
	}
	
	public byte [] decompress(byte [] msg, int size) throws DataFormatException
	{
		outBuff = new byte [size];
		inflater.setInput(msg);
		inflater.inflate(outBuff);
		inflater.reset();
		return outBuff;
	}
	
}
