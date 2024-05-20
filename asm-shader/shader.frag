const float PI = 3.1415926535;
const float FOV = 45.0; // Field of View (Camera Angle - higher views more in space, lower is like zooming in)
const float INF = 1e10; // clipping distance (infinity)
const float EPS = 1e-10; // epsilon (small distance)

const int SAMPLE = 2; // anti-aliasing (makes it less jagged)
const float OFF1 = 1.0 / float(SAMPLE); // offset within a pixel to get sample value (offset from uper-left corner)
const float OFF2 = OFF1 / 2.0; // offset within a pixel to get sample value (offset from uper-left corner)

struct Sphere {
    vec3 pos;
    float radius;
    vec3 color;
};

struct Ray {
    vec3 pos;
    vec3 dir;
};

struct HitInfo {
    vec3 color;
    vec3 normal; // perpendicular vector
    Ray ray;
    float t; // where the hit actually happened (hit is ray origin + (t * rayDir))
};

// Spheres in the scene
Sphere spheres[] = Sphere[](
    Sphere(vec3(-1.0, -0.75, -5.0), 0.8, vec3(1.0, 0.0, 0.0)),
    Sphere(vec3(1.0, -0.75, -5.0), 0.8, vec3(0.0, 1.0, 0.0)),
    Sphere(vec3(0.0, 0.75, -5.0), 0.8, vec3(0.0, 0.0, 1.0))
);

bool rayHitSphere(Ray r, Sphere s, out HitInfo info) {
    // p: new ray origin (sphere moved to 0)
    vec3 p = s.pos - r.pos;

    // raw direction
    vec3 d = r.dir;
    
    // Model ray as a complete line
    float dp = dot(d, p);
    float dd = dot(d, d);
    float pp = dot(p, p);
    
    // Sphere is treated as a radius
    // Inner part of square root for quadratic equation
    float inner = dp*dp - dd*(pp - s.radius*s.radius);

    // For weeding out pixel not on the sphere
    if (inner < 0.0) return false;
    
    // Want to find smallest t (so only have -)
    float t = (dp - sqrt(inner)) / dd;
    
    // Since hit is behind origin of ray, know it's not a hit
    if (t < 0.0) return false;

    info.color = s.color;
    info.normal = normalize(r.pos + t*r.dir - s.pos);
    info.ray = r;
    info.t = t;

    return true;
}

bool shootRay(Ray r, inout HitInfo info) {
    bool updated = false;

    // Loop through all the spheres
    for (int i = 0; i < spheres.length(); i++) {
        HitInfo sphere_info;

        // Use the less than to make sure you are only updating the closest sphere
        if (rayHitSphere(r, spheres[i], sphere_info) && sphere_info.t < info.t) {
            info = sphere_info;
            updated = true;
        }
    }
    return updated;
}

vec4 traceRay(Ray r) {
    HitInfo closest_info;

    // Set to large number to make sure it updates if there is a hit
    closest_info.t = INF;

    // Calculates where ray hits scene, keeps track of which hit is closest
    // Returns hit info
    shootRay(r, closest_info);
    
    if (closest_info.t == INF) {
        return vec4(0.5);
    }
    
    // Point light defined in time (going around in circle in time)
    vec3 light_pos = vec3(cos(iTime), 2.0, -5.0 + sin(iTime));
    
    vec3 hit_pos = r.pos + r.dir*closest_info.t;
    vec3 light_dir = normalize(light_pos - hit_pos);

    // cos(value you actually got) to get the color
    closest_info.color *= max(dot(closest_info.normal, light_dir), 0.0);
    
    // Calculate for dark shadows
    Ray light_ray = Ray(hit_pos + light_dir*EPS, light_dir);
    HitInfo light_info;
    light_info.t = INF;

    // If hits something when shooting towards light, know that the light actually isn't showing up
    if (shootRay(light_ray, light_info)) {
        return vec4(0.0);
    }

    return vec4(closest_info.color, 1.0);
}

// Input is the coordinate of the pixel on the screen (so mainImage is run for every pixel on the screen)
// Output is the color of the pixel
// vec4 is vector with 4 elements (x, y, z, w) = (R, G, B, alpha)
void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    // Initial color - all elements are 0
    vec4 color = vec4(0.0);

    // For loops are for the anti-aliasing
    for (int x = 0; x < SAMPLE; x++) {
        for (int y = 0; y < SAMPLE; y++) {
            // uv = value for something on camera plane (where in -1 to +1 range the sample should be)
            vec2 uv = (fragCoord + OFF2 + OFF1*vec2(x, y))/iResolution.xy;
            uv -= 0.5;
            uv.x *= iResolution.x / iResolution.y;
            uv *= 2.0 * tan(FOV * PI / 360.0);

            // Create a ray representing the sample (multiple samples per pixel, ray calculation for each sample)
            // Camera facing down -z axis
            // Normalize it to be length 1 so t is better defined
            vec3 rayDir = normalize(vec3(uv.x, uv.y, -1.0));

            // Ray starting point is 0
            // Ray ending point defined by sample location
            color += traceRay(Ray(vec3(0.0), rayDir));
        }
    }
    
    fragColor = color / float(SAMPLE * SAMPLE);
}