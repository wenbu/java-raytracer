package scene.lights.impl;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import scene.lights.Light;
import core.Intersection;
import core.Ray;
import core.colors.Color;
import core.math.Direction3;
import core.math.Point3;
import core.math.Transformation;

public class DirectionalLight extends Light
{
	private final Direction3 direction;

	private int numRays = 1;

	// angle from light ray to side of cone
	private double lightAngle = 0;

	private Random random = new Random();

	public DirectionalLight(Direction3 direction, Color color)
	{
		super(color);
		this.direction = Direction3.getNormalizedDirection(direction);
	}

	public DirectionalLight(Direction3 direction, Color color,
			int numShadowRays, double lightAngle)
	{
		super(color);
		this.direction = Direction3.getNormalizedDirection(direction);
		this.numRays = numShadowRays;
		this.lightAngle = lightAngle;
	}

	public DirectionalLight(Direction3 direction, Color ambientColor,
			Color diffuseColor, Color specularColor)
	{
		super(ambientColor, diffuseColor, specularColor);
		this.direction = Direction3.getNormalizedDirection(direction);
	}

	@Override
	public Set<Ray> getLightRay(Intersection intersection)
	{
		Set<Ray> lightRays = new HashSet<>();

		if (numRays == 1)
		{
			Point3 origin = intersection.getPosition();
			lightRays.add(new Ray(origin, direction.opposite()));
		}
		else
		{
			// i think this works
			Direction3 surfaceNormal = intersection.getNormal();
			Direction3 surfaceTangent = Direction3.getNormalizedDirection(
					surfaceNormal.z(), 0, -surfaceNormal.x());
			for (int i = 0; i < numRays; i++)
			{
				// generate random rotation axis perpendicular to normal
				double rotationAxisRotation = random.nextDouble() * 2 * Math.PI;
				Direction3 rotationAxis = Transformation.getRotation(
						surfaceNormal, rotationAxisRotation).transform(
						surfaceTangent);
				Direction3 rayDirection = Transformation.getRotation(
						rotationAxis, random.nextDouble() * lightAngle).transform(
						this.direction.opposite());
				lightRays
						.add(new Ray(intersection.getPosition(), rayDirection));
			}
		}
		return lightRays;
	}

}
