//
//  vec3 class
//
//  Copyright © 2020 Grant Damron. All rights reserved.
//

#ifndef VEC3_h
#define VEC3_h

#include "util.h"

#include <math.h>
#include <stdlib.h>
#include <iostream>

class vec3 {
public:
    vec3() : e{0.0, 0.0, 0.0} {}
    vec3(double e0, double e1, double e2) : e{e0, e1, e2} {}
    vec3(const vec3 &v) : e{v[0], v[1], v[2]} {}

    static vec3 random() {
        return vec3(
                random_double(),
                random_double(),
                random_double());
    }

    static vec3 random(double min, double max) {
        return vec3(
                random_double(min, max),
                random_double(min, max),
                random_double(min, max));
    }

    double x() const { return e[0]; }
    double y() const { return e[1]; }
    double z() const { return e[2]; }
    double r() const { return e[0]; }
    double g() const { return e[1]; }
    double b() const { return e[2]; }

    vec3 operator-() const { return vec3(-e[0], -e[1], -e[2]); }
    double operator[](int i) const { return e[i]; }
    double& operator[](int i) { return e[i]; }

    vec3& operator+=(const vec3 &vec) {
        e[0] += vec.e[0];
        e[1] += vec.e[1];
        e[2] += vec.e[2];
        return *this;
    }

    vec3& operator-=(const vec3 &vec) {
        e[0] -= vec.e[0];
        e[1] -= vec.e[1];
        e[2] -= vec.e[2];
        return *this;
    }

    vec3& operator*=(const double t) {
        e[0] *= t;
        e[1] *= t;
        e[2] *= t;
        return *this;
    }

    vec3& operator/=(const double t) {
        return *this *= 1.0/t;
    }

    double length_squared() const {
        return e[0] * e[0] + e[1] * e[1] + e[2] * e[2];
    }

    double length() const {
        return sqrt(length_squared());
    }

    vec3 normalized() const {
        auto v = vec3(*this);
        v /= length();
        return v;
    }

    void normalize() {
        *this /= length();
    }

private:
    double e[3];
};

// Type aliases for vec3
using point3 = vec3;
using color = vec3;

// utilities
inline std::ostream& operator<<(std::ostream &out, const vec3 &vec) {
    return out << vec[0] << ' ' << vec[1] << ' ' << vec[2];
}

inline vec3 operator+(const vec3 &v1, const vec3 &v2) {
    return vec3(
        v1[0] + v2[0],
        v1[1] + v2[1],
        v1[2] + v2[2]
    );
}

inline vec3 operator-(const vec3 &v1, const vec3 &v2) {
    return vec3(
        v1[0] - v2[0],
        v1[1] - v2[1],
        v1[2] - v2[2]
    );
}

inline vec3 operator*(const vec3 &v1, const vec3 &v2) {
    return vec3(
        v1[0] * v2[0],
        v1[1] * v2[1],
        v1[2] * v2[2]
    );
}

inline vec3 operator*(double t, const vec3 &v) {
    return vec3(v[0] * t, v[1] * t, v[2] * t);
}

inline vec3 operator*(const vec3 &v, double t) {
    return t * v;
}

inline vec3 operator/(vec3 v, double t) {
    return (1.0/t) * v;
}

inline double dot(const vec3 &v1, const vec3 &v2) {
    return v1[0] * v2[0] +
            v1[1] * v2[1] +
            v1[2] * v2[2];
}

inline vec3 cross(const vec3 &v1, const vec3 &v2) {
    return vec3(
        v1[1] * v2[2] - v1[2] * v2[1],
        v1[2] * v2[0] - v1[0] * v2[2],
        v1[0] * v2[1] - v1[1] * v2[0]
    );
}

inline vec3 random_unit_vector() {
    auto a = random_double(0, 2 * RT_PI);
    auto z = random_double(-1, 1);
    auto r = sqrt(1 - z * z);
    return vec3(r * cos(a), r * sin(a), z);
}

inline vec3 random_in_unit_sphere() {
    auto p = vec3::random(-1, 1);
    while(p.length_squared() >= 1) {
        p = vec3::random(-1, 1);
    }
    return p;
}

inline vec3 random_in_hemisphere(const vec3 &normal) {
    vec3 in_unit_sphere = random_in_unit_sphere();
    if (dot(in_unit_sphere, normal) > 0.0) {
        return in_unit_sphere;
    } else {
        return -in_unit_sphere;
    }
}

inline vec3 reflect(const vec3& vec, const vec3& norm) {
    return vec - 2 * dot(vec, norm) * norm;
}

inline vec3 refract(const vec3& uv, const vec3& norm, double eoe) {
    auto theta = dot(-uv, norm);
    vec3 r_perp = eoe * (uv + theta * norm);
    vec3 r_parallel = -sqrt(fabs(1.0 - r_perp.length_squared())) * norm;
    return r_perp + r_parallel;
}

inline vec3 random_in_unit_disk() {
    auto p = vec3(random_double(-1, 1), random_double(-1, 1), 0);
    while (p.length_squared() >= 1) {
        p = vec3(random_double(-1, 1), random_double(-1, 1), 0);
    }
    return p;
}

#endif
