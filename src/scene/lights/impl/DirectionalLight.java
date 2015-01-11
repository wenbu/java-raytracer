package scene.lights.impl;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import core.Intersection;
import core.Ray;
import core.colors.Color;
import core.math.Direction;
import core.math.Point;
import core.math.TransformationMatrix;
import core.math.VectorMath;
import scene.lights.Light;

public class DirectionalLight extends Light
{
	private final Direction direction;

	private int numRays = 1;

	// angle from light ray to side of cone
	private double lightAngle = 0;

	private Random random = new Random();

	public DirectionalLight(Direction direction, Color color)
	{
		super(color);
		this.direction = VectorMath.normalized(direction);
	}

	public DirectionalLight(Direction direction, Color color,
			int numShadowRays, double lightAngle)
	{
		super(color);
		this.direction = VectorMath.normalized(direction);
		this.numRays = numShadowRays;
		this.lightAngle = lightAngle;
	}

	public DirectionalLight(Direction direction, Color ambientColor,
			Color diffuseColor, Color specularColor)
	{
		super(ambientColor, diffuseColor, specularColor);
		this.direction = VectorMath.normalized(direction);
	}

	@Override
	public Set<Ray> getLightRay(Intersection intersection)
	{
		Set<Ray> lightRays = new HashSet<>();

		if (numRays == 1)
		{
			Point origin = intersection.getPosition();
			lightRays.add(new Ray(origin, direction.opposite()));
		}
		else
		{
			// i think this works
			Direction surfaceNormal = intersection.getNormal();
			Direction surfaceTangent = Direction.getNormalizedDirection(
					surfaceNormal.z(), 0, -surfaceNormal.x());
			for (int i = 0; i < numRays; i++)
			{
				// generate random rotation axis perpendicular to normal
				double rotationAxisRotation = random.nextDouble() * 2 * Math.PI;
				Direction rotationAxis = TransformationMatrix.getRotation(
						surfaceNormal, rotationAxisRotation).times(
						surfaceTangent);
				Direction rayDirection = TransformationMatrix.getRotation(
						rotationAxis, random.nextDouble() * lightAngle).times(
						this.direction.opposite());
				lightRays
						.add(new Ray(intersection.getPosition(), rayDirection));
			}
		}
		return lightRays;
	}

}
