#ifndef MATERIAL_H
#define MATERIAL_H

#include "includes.h"

struct hit_record;

class material {
    public:
        virtual bool scatter(
            const ray& r_in, const hit_record& record, color& atten, ray& scattered
        ) const = 0;
};

class lambertian : public material {
    public:
        lambertian(const color& a) : albedo(a) {}

        virtual bool scatter(
            const ray& r_in, const hit_record& rec, color& atten, ray& scattered
        ) const override {
            vec3 direction = rec.normal + random_unit_vector();
            scattered = ray(rec.point, direction);
            atten = albedo;
            return true;
        }

    private:
        color albedo;
};

class metal : public material {
    public:
        metal(const color& a): albedo(a), fuzz(0) {}
        metal(const color& a, double f): albedo(a), fuzz(f < 1 ? f : 1) {}

        virtual bool scatter(
            const ray& r_in, const hit_record& rec, color& atten, ray& scattered
        ) const override {
            vec3 toReflect = r_in.direction().normalized();
            vec3 reflected = reflect(toReflect, rec.normal);
            scattered = ray(rec.point, reflected + fuzz * random_in_unit_sphere());
            atten = albedo;
            return (dot(scattered.direction(), rec.normal) > 0);
        }

    private:
        color albedo;
        double fuzz;
};

class dielectric : public material {
    public:
        dielectric(double refraction) : index(refraction) {}

        virtual bool scatter(
            const ray& r_in, const hit_record& rec, color& atten, ray& scattered
        ) const override {
            atten = color(1.0, 1.0, 1.0);
            double refraction_ratio = index;
            if (rec.is_front_facing) {
                refraction_ratio = 1.0 / index;
            }

            vec3 norm = r_in.direction().normalized();
            double cos_theta = fmin(dot(-norm, rec.normal), 1.0);
            double sin_theta = sqrt(1.0 - cos_theta * cos_theta);
            bool cant_refract = refraction_ratio * sin_theta > 1.0;
            vec3 direction;

            if (cant_refract || reflectance(cos_theta, refraction_ratio) > random_double()) {
                direction = reflect(norm, rec.normal);
            } else {
                direction = refract(norm, rec.normal, refraction_ratio);
            }

            scattered = ray(rec.point, direction);
            return true;
        }

    private:
        double index;

        static double reflectance(double cosine, double ref_index) {
            auto r0 = (1 - ref_index) / (1 + ref_index);
            r0 = r0 * r0;
            r0 = r0 + (1 - r0) * pow((1 - cosine), 5);
            return r0;
        }
};

#endif
