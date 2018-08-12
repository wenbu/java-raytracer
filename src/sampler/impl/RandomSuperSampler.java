package sampler.impl;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import core.Pixel;
import core.Sample;
import core.math.Direction3;
import core.math.Point3;
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

		Point3 pixelLL = pixel.getPixelLL();
		Point3 pixelUL = pixel.getPixelUL();
		Point3 pixelLR = pixel.getPixelLR();

		Direction3 dx = pixelLR.minus(pixelLL);
		Direction3 dy = pixelUL.minus(pixelLL);

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
