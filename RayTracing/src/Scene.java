/* class Scene
 * Provides the structure for what is in the scene, and contains classes
 * for their rendering
 *
 * Doug DeCarlo
 */
import java.util.*;
import java.text.ParseException;
import java.lang.reflect.*;
import java.io.*;
import javax.vecmath.*;

class Scene
{
    // Scene elements
    Vector<Shape>    objects    = new Vector<Shape>();
    Vector<Light>    lights     = new Vector<Light>();
    Vector<Material> materials  = new Vector<Material>();
    Camera      camera     = null;
    MatrixStack MStack     = new MatrixStack();

    RGBImage    image      = null;

    // ------
    
    // Current insertion point in hierarchy for parser
    Vector<Shape> currentLevel;

    // Hierarchy enable (if off, "up" and "down" have no effect)
    // (Use this if you implement hierarchical object management or CSG)
    // (if you turn this on, you'll need to re-write intersects() and 
    // shadowTint() recursively for it to see the child objects!)
    boolean hierarchyOn    = false;
    
    // ------

    // Maximum recursion depth for a ray
    double recursionDepth  = 3;
    
    // Minimum t value in intersection computations
    double epsilon         = 1e-5;
    
    // Constructor
    public Scene()
        throws ParseException, IOException, NoSuchMethodException,
        ClassNotFoundException,IllegalAccessException,
        InvocationTargetException
    {
        // Set hierarchy at top level
        currentLevel = objects;

        // Add default material
        materials.addElement(new Material("default"));
    }

    //-----------------------------------------------------------------------

    /** render an image of size width X height */
    public RGBImage render(int width, int height, boolean verbose)
        throws ParseException, IOException, NoSuchMethodException,
        ClassNotFoundException,IllegalAccessException,
        InvocationTargetException
    {
        // Set up camera for this image resolution
        camera.setup(width, height);
        //System.out.println("Near plane: " + camera.near);
        //System.out.println(camera.pixelRay(0,0));

        // Make a new image
        image = new RGBImage(width, height);

        // Ray trace every pixel -- the main loop
        for (int i = 0; i < image.getWidth(); i++) {
            if (verbose)
              System.out.print("Rendering " +
                               (int)(100.0*i/(image.getWidth()-1)) + "%\r");

            for (int j = 0; j < image.getHeight(); j++) {
                // Compute (x,y) coordinates of pixel in [-1, 1]
                double x = ((double)i)/(image.getWidth()  - 1) * 2 - 1;
                double y = ((double)j)/(image.getHeight() - 1) * 2 - 1;
	       
                // Compute ray at pixel (x,y)
                Ray r = camera.pixelRay(x, y);
	       
                // Compute resulting color at pixel (x,y)
                Vector3d color = castRay(r, 0);
	       
                // Set color in image
                image.setPixel(i,j, color);
            }
        }

        if (verbose) {
            System.out.println();
            System.out.println("Done!");
        }

        return image;
    }

    /** compute pixel color for ray tracing computation for ray r
     *  (at a recursion depth)
     */
    private Vector3d castRay(Ray r, int depth)
    {
        Vector3d color = new Vector3d();
        ISect isect = new ISect();

        // Check if the ray hit any object (or recursion depth was exceeded)
        if (depth <= recursionDepth && intersects(r, isect)) {
            // -- Ray hit object as specified in isect
        	
            Material mat = isect.getHitObject().getMaterialRef();
            
            // -- Compute contribution to this pixel for each light by doing
            //    the lighting computation there (sending out a shadow feeler
            //    ray to see if light is visible from intersection point)
            
            for (int i = 0; i < lights.size(); i++) {
            	Light light = lights.get(i);
            	//System.out.println(isect.getHitPoint());
            	Vector3d tint = shadowRay(isect, light);
            	//System.out.println(tint);
            	color.add(light.compute(isect, tint, r));
            	
            	
            }

            /*if (depth < this.recursionDepth && color.x > 0.001 && color.y > 0.001 && color.z > 0.001) {
            	Shape shape = isect.getHitObject();
            	Vector3d reflect = new Vector3d();
            	
            	shape.getInvMatrix().transform(r.getPoint());
            	shape.getInvMatrix().transform(r.getDirection());
            	//Vector3d refract = new Vector3d();
            	Vector3d l = new Vector3d(r.getPoint().x-isect.hitPoint.x, r.getPoint().y-isect.hitPoint.y, r.getPoint().z-isect.hitPoint.z);
            	l.normalize();
            	isect.getNormal().normalize();
            	Tools.reflect(reflect, l, isect.getNormal());
            	//System.out.println(reflect);
            	shape.getMatrix().transform(r.getPoint());
            	shape.getMatrix().transform(r.getDirection());
            	
            	reflect.set(castRay(new Ray(isect.getHitPoint(),reflect), depth+1));
            	Tools.termwiseMul3d(reflect, isect.getHitObject().getMaterialRef().getKs());
            	color.add(reflect);
            }*/
            //color.set(mat.getKd());
        }
        
        
        return color;
    }

    /** determine the closest intersecting object along ray r (if any) 
     *  and its intersection point
     */
    private boolean intersects(Ray r, ISect intersection)
    {
    	//System.out.println("r in: " + r);
    	double shortestDistance = 0.0;
    	int minIndex = 0;
    	boolean retval = false;
    	boolean minDistSet = false;
    	Shape closestShape = null;
    	
        // For each object
        for (int i = 0; i < objects.size(); i++) {
            Shape current = (Shape) objects.get(i);
            // transform ray into object space
            current.getInvMatrix().transform(r.getDirection());
            current.getInvMatrix().transform(r.getPoint());
            // check for hit
            if (current.hit(r, intersection, true, epsilon)) {
            	retval=true;
            	Vector3d d = new Vector3d(r.getPoint().x-intersection.getHitPoint().x,
            							  r.getPoint().y-intersection.getHitPoint().y,
            							  r.getPoint().z-intersection.getHitPoint().z);
            	double dist = d.length();
            	// if object is closer than others, take note
            	if ((dist < shortestDistance) || !minDistSet) {
            		minDistSet = true;
            		shortestDistance = dist;
            		minIndex = i;
            	}
            	
            }
            // transform ray back into world space
            current.getMatrix().transform(r.getPoint());
            current.getMatrix().transform(r.getDirection());
            
        }
        
        if (retval==true) {
        	closestShape = objects.get(minIndex);
        	// transform ray into object space
        	closestShape.getInvMatrix().transform(r.getDirection());
            closestShape.getInvMatrix().transform(r.getPoint());
            // get the intersection
        	closestShape.hit(r, intersection, false, epsilon);
            // transform intersection and ray back into world space
        	intersection.getNormal().normalize();
        	closestShape.getMatrix().transform(intersection.getHitPoint());
        	closestShape.getInvTMatrix().transform(intersection.getNormal());
        	r.getDirection().scale(intersection.getT());
        	closestShape.getMatrix().transform(r.getDirection());
            closestShape.getMatrix().transform(r.getPoint());
        }
        return retval;
    }

    /** compute the amount of unblocked color that is let through to
     *  a given intersection, for a particular light
     *
     *  If the light is entirely blocked, return (0,0,0), not blocked at all
     *  return (1,1,1), and partially blocked return the product of Kt's
     *  (from transparent objects)
     */
    Vector3d shadowRay(ISect intersection, Light light)
    {
        Vector3d lightVec;
        Point3d hitPoint = new Point3d(intersection.getHitPoint());
        
        if (light.isDirectional()) {
        	lightVec = new Vector3d(light.direction);
        	//lightVec.normalize();
        	return shadowTintDirectional(new Ray(hitPoint, lightVec));
        } else {
        	lightVec = new Vector3d(light.position.x-hitPoint.x, light.position.y-hitPoint.y, light.position.z-hitPoint.z);
        	//lightVec.normalize();
        	return shadowTint(new Ray(hitPoint, lightVec), 200.0);
        }
    }

    /** determine how the light is tinted along a particular ray which
     *  has no maximum distance (i.e. from a directional light)
     */
    private Vector3d shadowTintDirectional(Ray r)
    {
        return shadowTint(r, Double.MAX_VALUE);
    }

    /** determine how the light is tinted along a particular ray, not
     *  considering intersections further than maxT
     */
    private Vector3d shadowTint(Ray r, double maxT)
    {
    	boolean wasHit = false;
        Vector3d tint = new Vector3d(0.0, 0.0, 0.0);
        ISect intersection = new ISect();
        // For each object
        Enumeration e = objects.elements();
        while (e.hasMoreElements()) {
        	Shape current = (Shape) e.nextElement();
        	current.getInvMatrix().transform(r.getDirection());
            current.getInvMatrix().transform(r.getPoint());
            if (current.hit(r, intersection, false, epsilon)) {
            	wasHit = true;
            	//Tools.termwiseMul3d(tint, intersection.getHitObject().getMaterialRef().getKt());
            	//tint.set(0,0,0);
            	tint.add(intersection.getHitObject().getMaterialRef().getKt());
            }
            current.getMatrix().transform(r.getDirection());
            current.getMatrix().transform(r.getPoint());
        }
        
        if (!wasHit) tint.set(1.0,1.0,1.0);
        
        return tint;
    }

    //------------------------------------------------------------------------

    /** Fetch a material by name */
    Material getMaterial(String name)
    {
        // Unspecified material gets default
        if (name == null || name.length() == 0)
          name = new String("default");

        // Find the material with this name
        for (int i = 0; i < materials.size(); i++){
            Material mat = (Material)materials.elementAt(i);

            if (mat.getName().compareTo(name) == 0) {
                return mat;
            }
        }

        throw new RuntimeException("Undefined material " + name);
    }

    /** Add a new scene element */
    public void addObject(RaytracerObject newItem)
    {
        if (newItem instanceof Light) {
            Light l = (Light)newItem;

            l.transform(MStack.peek());

            lights.addElement(l);
        } else if (newItem instanceof Material) {
            Material m = (Material)newItem;

            materials.addElement(m);
        } else if (newItem instanceof Shape) {
            Shape s = (Shape)newItem;

            s.parent = currentLevel;
            s.setMaterialRef(getMaterial(s.getMaterialName()));
            s.setMatrix(MStack.peek());

            currentLevel.addElement(s);
        }
        else if (newItem instanceof Camera){
            camera = (Camera)newItem;
        }
    }

    /** Set up the scene (called after the scene file is read in) */
    public void setup()
        throws ParseException, IOException, NoSuchMethodException,
        ClassNotFoundException,IllegalAccessException,
        InvocationTargetException
    {
        // Specify default camera if none specified in scene file
        if (camera == null)
          camera = new Camera();

        // Set up materials
        for (int i = 0; i < materials.size(); i++){
            Material mat = (Material)materials.elementAt(i);
            mat.setup(Trace.verbose);
        }
    }

    //-------------------------------------------------------------------------

    // accessors
    public RGBImage getImage() { return image; }
    public void setImage(RGBImage newImage) { image = newImage; }
    public MatrixStack getMStack()  { return MStack; }
}
