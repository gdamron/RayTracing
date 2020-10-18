#ifndef HITTABLE_H
#define HITTABLE_H

#include "includes.h"
#include "ray.h"

class material;

struct hit_record {
    point3 point;
    vec3 normal;
    shared_ptr<material> material_ptr;
    double t;
    bool is_front_facing;

    inline void set_face_normal(const ray& r, const vec3& outward_norm) {
        is_front_facing = dot(r.direction(), outward_norm) < 0;
        if (is_front_facing) {
            normal = outward_norm;
        } else {
            normal = -outward_norm;
        }
    }
};

class hittable {
    public:
        virtual bool hit(const ray &r, double t_min, double t_max, hit_record &rec) const = 0;
};

#endif
