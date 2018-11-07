package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.colors.Colors;
import core.colors.RGBSpectrum;
import core.math.Transformation;
import scene.geometry.impl.Sphere;
import scene.materials.Material;
import scene.primitives.Primitive;
import scene.primitives.impl.GeometricPrimitive;
import utilities.MaterialUtilities;

public class PdbReader
{
    private final String filePath;
    private final double radiusMultiplier;
    
    private static class ElementInfo
    {
        private final RGBSpectrum color;
        private final double radius;
        
        public ElementInfo(double r, double g, double b, double radius)
        {
            color = new RGBSpectrum(r, g, b);
            this.radius = radius;
        }

        public RGBSpectrum getColor()
        {
            return color;
        }

        public double getRadius()
        {
            return radius;
        }
    }
    
    private static Map<String, ElementInfo> elements = new HashMap<>();
    static
    {
        elements.put("H", new ElementInfo(0.9, 0.9, 0.9, 0.25));
        elements.put("C", new ElementInfo(0.2, 0.2, 0.2, 0.7));
        elements.put("N", new ElementInfo(0.1, 0.1, 0.9, 0.65));
        elements.put("O", new ElementInfo(0.9, 0.1, 0.1, 0.6));
        elements.put("P", new ElementInfo(0.7, 0.5, 0.1, 1));
    }
    
    public PdbReader(String filePath, double radiusMultiplier)
    {
        this.filePath = filePath;
        this.radiusMultiplier = radiusMultiplier;
    }
    
    public List<Primitive> read(Transformation sceneTransform) throws FileNotFoundException, IOException
    {
        List<Primitive> primitives = new LinkedList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] splitLine = line.split(" +");
                if (!splitLine[0].equalsIgnoreCase("ATOM"))
                {
                    continue;
                }
                
                /*
                 * 0 -- "ATOM"
                 * 1 -- atom id
                 * 2 -- atom name
                 * 3 -- residue name
                 * 4 -- chain identifier
                 * 5 -- residue sequence number
                 * 6 -- position(x)
                 * 7 -- position(y)
                 * 8 -- position(z)
                 * 9 -- occupancy
                 * 10 -- temperature factor (?)
                 * 11 -- element symbol
                 */
                double x = Double.valueOf(splitLine[6]);
                double y = Double.valueOf(splitLine[7]);
                double z = Double.valueOf(splitLine[8]);
                String element = splitLine[11];
                
                ElementInfo elementInfo = elements.get(element);
                if (elementInfo == null)
                {
                    throw new RuntimeException("Unknown element " + element);
                }
                
                RGBSpectrum color = elementInfo.getColor();
                double radius = elementInfo.getRadius();
                
                Transformation transform = sceneTransform.compose(Transformation.getTranslation(x, y, z));
                Sphere sphere = new Sphere(transform, transform.inverse(), false, radius * radiusMultiplier);
                Material material = MaterialUtilities.getPlasticMaterial(color, Colors.WHITE, 0.1, false);
                Primitive primitive = new GeometricPrimitive(sphere, material);
                
                primitives.add(primitive);
            }
        }
        return primitives;
    }
}
