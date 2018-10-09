package scene.medium;

import core.Ray;
import core.colors.RGBSpectrum;
import sampler.Sampler;

public interface Medium
{
    RGBSpectrum transmittance(Ray ray, Sampler sampler);
    
    public static class MediumInterface
    {
        private final Medium inside;
        private final Medium outside;
        
        public MediumInterface()
        {
            this(null);
        }
        
        public MediumInterface(Medium medium)
        {
            this(medium, medium);
        }
        
        public MediumInterface(Medium inside, Medium outside)
        {
            this.inside = inside;
            this.outside = outside;
        }
        
        public boolean isMediumTransition()
        {
            return inside != outside;
        }
        
        public Medium getInside()
        {
            return inside;
        }
        
        public Medium getOutside()
        {
            return outside;
        }
    }
}
