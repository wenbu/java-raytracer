package sampler.impl;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import core.Pixel;
import core.Sample;
import core.math.Direction;
import core.math.Point;
import sampler.Sampler;

public class RandomSuperSampler implements Sampler
{
	private final int numSamples;
	private final Random random;

	public RandomSuperSampler(int numSamples)
	{
		this.numSamples = numSamples;
		random = new Random();
	}

	@Override
	public Set<Sample> getSamples(Pixel pixel)
	{
		Set<Sample> samples = new HashSet<>();

		Point pixelLL = pixel.getPixelLL();
		Point pixelUL = pixel.getPixelUL();
		Point pixelLR = pixel.getPixelLR();

		Direction dx = pixelLR.minus(pixelLL);
		Direction dy = pixelUL.minus(pixelLL);

		for (int i = 0; i < numSamples; i++)
		{
			double x = random.nextDouble();
			double y = random.nextDouble();

			samples.add(new Sample(pixelLL.plus(dx.times(x))
					                      .plus(dy.times(y))));
		}
		
		return samples;
	}

}
