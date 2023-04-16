#include "includes.h"

#include "nlohmann/json.h"
#include "camera.h"
#include "color.h"
#include "hittable_list.h"
#include "material.h"
#include "sphere.h"

#include <string.h>
#include <iostream>
#include <fstream>

using namespace std;

color ray_color(const ray &r, const hittable &world, int depth) {
    if (depth <= 0) {
        return color(0, 0, 0);
    }

    hit_record record;
    if (world.hit(r, 0.001, RT_INFINITY, record)) {
        ray scattered;
        color attenuation;
        if (record.material_ptr->scatter(r, record, attenuation, scattered)) {
            return attenuation * ray_color(scattered, world, depth-1);
        }
        return color(0,0,0);
    }

    // get unit vector
    vec3 vec = r.direction().normalized();

    // map y value to 0-1;
    auto t = 0.5 * (vec.y() + 1.0);

    // interpolate white to blue
    return (1.0 - t) * color(1.0, 1.0, 1.0) + t * color(0.5, 0.7, 1.0);
}

hittable_list generate_world() {
    hittable_list world;

    auto ground_mat = make_shared<lambertian>(color(0.5, 0.5, 0.5));
    world.add(make_shared<sphere>(point3(0, -1000, 0), 1000, ground_mat));

    const int N = 11;
    for (int i = -N; i < N; i++) {
        for (int j = -N; j < N; j++) {
            auto rand_mat = random_double();
            point3 center(i + 0.9 * random_double(), 0.2, j + 0.9 * random_double());

            if ((center - point3(4, 0.2, 0)).length() > 0.9) {
                shared_ptr<material> mat;

                if (rand_mat < 0.8) {
                    // diffuse
                    auto albedo = color::random() * color::random();
                    mat = make_shared<lambertian>(albedo);
                } else if (rand_mat < 0.95) {
                    // metal
                    auto albedo = color::random(0.5, 1);
                    auto fuzz = random_double(0, 0.5);
                    mat = make_shared<metal>(albedo, fuzz);
                } else {
                    // glass
                    mat = make_shared<dielectric>(1.5);
                }

                world.add(make_shared<sphere>(center, 0.2, mat));
            }
        }
    }
    auto mat1 = make_shared<dielectric>(1.5);
    world.add(make_shared<sphere>(point3(0, 1, 0), 1.0, mat1));

    auto mat2 = make_shared<lambertian>(color(0.4, 0.2, 0.1));
    world.add(make_shared<sphere>(point3(-4, 1, 0), 1.0, mat2));

    auto mat3 = make_shared<metal>(color(0.7, 0.6, 0.5), 0.0);
    world.add(make_shared<sphere>(point3(4, 1, 0), 1.0, mat3));

    return world;
}

int main(int argc, char** argv) {
    nlohmann::json config;
    for (int i = 0; i < argc; i++) {
        auto arg = argv[i];
        if (strcmp(arg, "--config") == 0 && i < argc - 1) {
            auto file_name = argv[i + 1];
            std::ifstream ifs(file_name);
            ifs >> config;
        }
    }

    cout << config["test"] << endl;;
    return 0;

    // image settings
    const auto aspect_ratio = 16.0 / 9.0;
    const int width = 400;
    const int height = static_cast<int>(width / aspect_ratio);
    const int samples = 100;
    const int depth = 50;

    // create world
    auto world = generate_world();

    // create camera
    point3 lookfrom(13, 2, 3);
    point3 lookat(0, 0, 0);
    vec3 vup(0, 1, 0);
    auto dist_to_focus = 10.0;
    auto aperture = 0.1;
    auto fov = 20.0;
    camera camera(
            lookfrom,
            lookat,
            vup,
            fov,
            aspect_ratio,
            aperture,
            dist_to_focus);

    // render the image
    cout << "P3\n" << width << " " << height << "\n255\n";
    for (int j = height - 1; j >= 0; j--) {
    cerr << "\rlines remaining: " << j << ' ' << flush;
        for (int i = 0; i < width; i++) {
            color col(0, 0, 0);
            for (int k = 0; k < samples; k++) {
                auto u = (i + random_double()) / (width - 1);
                auto v = (j + random_double()) / (height - 1);
                ray r = camera.get_ray(u, v);
                col += ray_color(r, world, depth);
            }
            write_color(cout, col, samples);
        }
    }

    cerr << "\nDone\n";
}
