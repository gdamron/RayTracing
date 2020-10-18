#ifndef CAMERA_H
#define CAMERA_H

#include "includes.h"

class camera {
    public:
        camera(
            point3 lookfrom,
            point3 lookat,
            vec3 vup,
            double vfov,
            double aspect_ratio,
            double aperture,
            double focus_dist
        ) {
            auto theta = degrees_to_rads(vfov);
            auto h = tan(theta / 2);
            auto viewport_height = 2.0 * h;
            auto viewport_width = aspect_ratio * viewport_height;

            w = (lookfrom - lookat).normalized();
            u = cross(vup, w).normalized();
            v = cross(w, u);

            origin = lookfrom;
            horizontal = focus_dist * viewport_width * u;
            vertical = focus_dist * viewport_height * v;
            lower_left = origin - horizontal / 2 - vertical / 2 - focus_dist * w;

            lens_radius = aperture / 2;
        }

        ray get_ray(double s, double t) const {
            vec3 rd = lens_radius * random_in_unit_disk();
            vec3 offset = u * rd.x() + v * rd.y();

            vec3 dir = lower_left + s * horizontal + t * vertical - origin - offset;
            return ray(origin + offset, dir);
        }

    private:
        point3 origin;
        point3 lower_left;
        vec3 horizontal;
        vec3 vertical;
        vec3 u;
        vec3 v;
        vec3 w;
        double lens_radius;
};

#endif
