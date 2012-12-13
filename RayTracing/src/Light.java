/* class Light
 * Description of a light source
 *
 * Doug DeCarlo
 */
import java.io.*;
import java.text.ParseException;
import java.lang.reflect.*;
import javax.vecmath.*;

class Light extends RaytracerObject
{
    final public static String keyword = "light";
    
    /** parameters of the light source */

    // Location of light (one of these is always null), depending on
    // whether the light is directional light or not
    Point3d  position    = new Point3d();
    Vector3d direction   = null;

    // Light color (R, G, B)
    Vector3d color       = new Vector3d(1.0, 1.0, 1.0);

    // Light attenuation (Kc, Kl, Kq); default has no attenuation
    //    - given distance D from a light (not directional)
    //    - atten factor is 1/(Kc + Kl * D + Kq * D^2)
    //    - ambient light is not attenuated
    Vector3d attenuation = new Vector3d(1.0, 0.0, 0.0);

    //------------------------------------------------------------------------

    /** constructor that reads the content of the object from the tokenizer */
    public Light(StreamTokenizer tokenizer)
        throws ParseException, IOException, NoSuchMethodException,
        ClassNotFoundException,IllegalAccessException,
        InvocationTargetException
    {
        super(tokenizer);

        // add the parameters
        addSpec("position",     "setPosition",
                position.getClass().getName());
        addSpec("direction",    "setDirection", 
                (new Vector3d()).getClass().getName());
        addSpec("color",        "setColor",
                color.getClass().getName());
        addSpec("attenuation",  "setAttenuation",
                attenuation.getClass().getName());

        // read the content of this object
        read(tokenizer);
    }

    /** transform light location given matrix m */
    public void transform(Matrix4d m)
    {
        if (isDirectional()) {
            m.transform(direction);
        } else {
            m.transform(position);
        }
    }

    //------------------------------------------------------------------------

    // accessors
    public Point3d  getPosition()    { return position;    }
    public Vector3d getDirection()   { return direction;   }
    public Vector3d getColor()       { return color;       }
    public Vector3d getAttenuation() { return attenuation; }

    public void setPosition(Point3d p)
    {
        if (position != null)
          position.set(p);
        else
          position = new Point3d(p);

        direction = null;
    }

    public void setDirection(Vector3d d)
    {
        if (direction != null)
          direction.set(d);
        else
          direction = new Vector3d(d);
	
        // Direction is always normalized
        direction.normalize();
	
        position = null;
    }

    public void setColor(Vector3d c)        { color = c; }
    public void setAttenuation (Vector3d a) { attenuation = a; }

    /** For determining whether light is directional or position-based */
    public boolean isDirectional() { return direction != null; }

    /** For printing light specification */
    public void print(PrintStream out)
    {
        super.print(out);

        if (direction != null)
          out.println("Direction   : " + direction   );
        if (position != null)
          out.println("Position    : " + position   );
        out.println("Color       : " + color       );
        out.println("Attenuation : " + attenuation );
    }

    //------------------------------------------------------------------------

    /** compute the resulting color at an intersection point for
     *  _this_ light, which has been tinted (from shadowing), and given
     *  the ray that led to the intersection (which can be traced back
     *  to the camera location)
     *
     * The computation does the following:
     *  - computes ambient, diffuse and specular (Phong model) illumination
     *  - uses material color (Ka, Kd, Ks) and texture
     *  - handles both directional and (attenuated) point light sources
     *    (which are affected by shadows using 'tint')
     *
     * The 'tint' is used to specify the amount of the light that is being
     * let through to the intersection point.  An exposed light has a tint
     * of (1,1,1) and an occluded light has a tint of (0,0,0) -- intermediate
     * values can result from intervening transparent objects.
     * The tint does not affect the ambient light.
     */
    Vector3d compute(ISect intersection, Vector3d tint, Ray r)
    {
        // Material for this object
        Material mat = intersection.getHitObject().getMaterialRef();
        Point3d hitPoint = new Point3d(intersection.getHitPoint());
        //intersection.getHitObject().getMatrix().transform(hitPoint);
        //intersection.getHitObject().getInvTMatrix().transform(intersection.getNormal());
        Vector3d l_vec;
        if (this.isDirectional()) {
        	l_vec = new Vector3d(this.direction);
        } else {
        	l_vec = new Vector3d(this.position.x-hitPoint.x, this.position.y-hitPoint.y, this.position.z-hitPoint.z);
        }
        
        Vector3d n_vec = new Vector3d(intersection.getNormal());
        intersection.getHitObject().getInvTMatrix().transform(n_vec);
        Vector3d r_vec = new Vector3d();
        Vector3d v_vec = new Vector3d(r.getPoint().x-hitPoint.x, r.getPoint().y-hitPoint.y, r.getPoint().z-hitPoint.z);
        Vector3d dist_vec = new Vector3d(l_vec);
        Vector3d atten_vec = new Vector3d(1/ (attenuation.x + attenuation.y*dist_vec.x + attenuation.z*(dist_vec.x*dist_vec.x)),
        								  1/ (attenuation.x + attenuation.y*dist_vec.y + attenuation.z*(dist_vec.y*dist_vec.y)),
        								  1/ (attenuation.x + attenuation.y*dist_vec.z + attenuation.z*(dist_vec.z*dist_vec.z)));
        //double distance = l_vec.length();
       
        n_vec.normalize();
        l_vec.normalize();
        v_vec.normalize();
        Tools.reflect(r_vec, l_vec, n_vec);
        r_vec.normalize();
        
        double n_dot_l = n_vec.dot(l_vec);
        double r_dot_v = r_vec.dot(v_vec);
        
        // ambient
        Vector3d ambient = new Vector3d(mat.getKa());
        
        // diffuse
        Vector3d diffuse = new Vector3d(mat.getKd());
        Tools.termwiseMul3d(diffuse, atten_vec);
        Tools.termwiseMul3d(diffuse, tint);
        diffuse.scale(Math.max(0, n_dot_l));
        
        //specular
        Vector3d specular = new Vector3d(mat.getKs());
        Tools.termwiseMul3d(specular, atten_vec);
        Tools.termwiseMul3d(specular, tint);
        double shininess = Math.pow(Math.max(0, r_dot_v), mat.getShiny());
        specular.scale(shininess);
        
        // scale by T(u,v) if material has texture
        if (mat.hasTexture()) {
        	Vector3d Tu = new Vector3d(mat.getTextureColor(intersection.getU(), intersection.getV()));
        	Tools.termwiseMul3d(ambient, Tu);
        	Tools.termwiseMul3d(diffuse, Tu);
        }
        
        System.out.println(specular);
        
        // Compute color
        Vector3d rgb = new Vector3d(ambient);
        rgb.add(diffuse);
        rgb.add(specular);
        
        return rgb;
    }
}
