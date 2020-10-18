#ifndef SPHERE_H
#define SPHERE_H

#include <math.h>
#include "hittable.h"
#include "vec3.h"

class sphere : public hittable {
    public:
        sphere() {}
        sphere(point3 cntr, double rad, shared_ptr<material> mat)
            : center_(cntr), radius_(rad), material_ptr(mat) {}

        point3 center() const { return center_; }
        double radius() const { return radius_; }
        shared_ptr<material> shared_material() const { return material_ptr; }
        virtual bool hit(const ray &r, double t_min, double t_max, hit_record &rec) const override;

    private:
        point3 center_;
        double radius_;
        shared_ptr<material> material_ptr;
};

bool sphere::hit(const ray &r, double t_min, double t_max, hit_record &rec) const {
    vec3 toSphere = r.origin() - center_;
    vec3 dir = r.direction();
    auto a = dir.length_squared();
    auto half_b = dot(toSphere, dir);
    auto c = toSphere.length_squared() - radius_ * radius_;
    auto discriminant = half_b * half_b - a * c;

    if (discriminant > 0) {
        auto root = sqrt(discriminant);
        auto t = (-half_b - root) / a;
        if (t < t_max && t > t_min) {
            rec.t = t;
            rec.point = r.at(t);
            vec3 outward_norm  = (rec.point - center_) / radius_;
            rec.set_face_normal(r, outward_norm);
            rec.material_ptr = material_ptr;
            return true;
        }

        t = (-half_b + root) / a;
        if (t < t_max && t > t_min) {
            rec.t = t;
            rec.point = r.at(t);
            vec3 outward_norm  = (rec.point - center_) / radius_;
            rec.set_face_normal(r, outward_norm);
            rec.material_ptr = material_ptr;
            return true;
        }
    }

    return false;
}

#endif
