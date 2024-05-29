#define USE_ASM

const float PI = 3.1415926535;
const float FOV = 45.0;
const float INF = 1e10;
const float EPS = 1e-10;

const int SAMPLE = 2;
const float OFF1 = 1.0 / float(SAMPLE);
const float OFF2 = OFF1 / 2.0;

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
    vec3 normal;
    Ray ray;
    float t;
};


#define INST00(name, expr) void name (out vec3  dst, in vec3 src1, in vec3  src2) { dst = expr; }
#define INST01(name, expr) void name (out float dst, in vec3 src1, in vec3  src2) { dst = expr; }
#define INST10(name, expr) void name (out vec3  dst, in vec3 src1, in float src2) { dst = expr; }
#define INST11(name, expr) void name (out float dst, in vec3 src1, in float src2) { dst = expr; }

#define INSTSS(name, expr) void name (out float dst, in float src1, in float src2) { dst = expr; }

#define INSTIM(name, expr) void name (inout vec3 dst, in float src1, in float imm) { expr; }


// ASM INSTRUCTIONS

INST00(add, src1 + src2)
INST00(sub, src1 - src2)
INST00(norm, src1 / length(src1))

INST01(mag, length(src1))
INST01(mag2, dot(src1, src1))
INST01(dot_, dot(src1, src2))

INST10(smult, src1 * src2)
INST10(sdiv, src1 / src2)

void sqrt_(out float dst, in vec3 src1, in float src2) {
    if (src2 < 0.0) dst = INF;
    else dst = sqrt(src2);
}

void addi(in float src1, in float imm, out float dst) { dst = src1 + imm; }

INSTSS(add,  src1 + src2)
INSTSS(sub,  src1 - src2)
INSTSS(mult, src1 * src2)
INSTSS(div,  src1 / src2)

INSTIM(vaddix, dst.x = src1 + imm)
INSTIM(vaddiy, dst.y = src1 + imm)
INSTIM(vaddiz, dst.z = src1 + imm)

// PSEUDO INSTS

#define vmov(src, dst) add(src, vreg[0], dst)
#define smov(src, dst) add(src, sreg[0], dst)

//


Sphere spheres[] = Sphere[](
    Sphere(vec3(-1.0, -0.75, -5.0), 0.8, vec3(1.0, 0.0, 0.0)),
    Sphere(vec3(1.0, -0.75, -5.0), 0.8, vec3(0.0, 1.0, 0.0)),
    Sphere(vec3(0.0, 0.75, -5.0), 0.8, vec3(0.0, 0.0, 1.0))
);

// inputs
// vreg[1] = ray origin
// vreg[2] = ray direction
// vreg[3] = sphere position
// sreg[1] = sphere radius
//
// outputs
// vreg[1] = normal vector
// sreg[1] = t
void rayHitSphereAsm(inout vec3 vreg[16], inout float sreg[16]) {
    // p = s.pos - r.pos;
    sub(vreg[4], vreg[3], vreg[1]);
    // d = r.dir;
    //vreg[5] = vreg[2];
    
    // dp = dot(d, p)
    dot_(sreg[2], vreg[4], vreg[2]);
    // pp = dot(p, p)
    dot_(sreg[3], vreg[4], vreg[4]);
    
    // s.radius * s.radius
    mult(sreg[4], sreg[1], sreg[1]);
    // pp - s.radius*s.radius
    sub(sreg[5], sreg[3], sreg[4]);
    // dp*dp
    mult(sreg[6], sreg[2], sreg[2]);
    // inner = dp*dp - (pp - s.radius*s.radius)
    sub(sreg[7], sreg[6], sreg[5]);
    
    // sqrt(inner)
    sqrt_(sreg[8], vreg[0], sreg[7]);
    // t = dp - sqrt(inner)
    sub(sreg[1], sreg[2], sreg[8]);
    
    // t*d
    smult(vreg[6], vreg[2], sreg[1]);
    // t*d - p
    sub(vreg[7], vreg[6], vreg[4]);
    // normal = normalize(t*d - p)
    norm(vreg[1], vreg[7], vreg[0]);
}

bool rayHitSphereAsmWrap(Ray r, Sphere s, out HitInfo info) {
    vec3 vreg[16];
    float sreg[16];
    vreg[0] = vec3(0.0);
    sreg[0] = 0.0;
    
    vreg[1] = r.pos;
    vreg[2] = r.dir;
    vreg[3] = s.pos;
    sreg[1] = s.radius;
    
    rayHitSphereAsm(vreg, sreg);
    
    float t = sreg[1];
    vec3 normal = vreg[1];
    
    if (t < 0.0) return false;
    
    info.color = s.color;
    info.normal = normal;
    info.ray = r;
    info.t = t;

    return true;
}

bool rayHitSphere(Ray r, Sphere s, out HitInfo info) {
    vec3 p = s.pos - r.pos;
    vec3 d = r.dir;
    
    float dp = dot(d, p);
    float pp = dot(p, p);
    
    float inner = dp*dp - (pp - s.radius*s.radius);
    if (inner < 0.0) return false;
    
    float t = (dp - sqrt(inner));
    
    if (t < 0.0) return false;
    
    info.color = s.color;
    info.normal = normalize(t*d - p);
    info.ray = r;
    info.t = t;
    
    return true;
}

bool shootRay(Ray r, inout HitInfo info) {
    bool updated = false;
    for (int i = 0; i < spheres.length(); i++) {
        HitInfo sphere_info;
        if (
        #ifdef USE_ASM
        rayHitSphereAsmWrap(r, spheres[i], sphere_info)
        #else
        rayHitSphere(r, spheres[i], sphere_info)
        #endif
        && sphere_info.t < info.t
        ) {
            info = sphere_info;
            updated = true;
        }
    }
    return updated;
}

vec4 traceRay(Ray r) {
    HitInfo closest_info;
    closest_info.t = INF;
    shootRay(r, closest_info);
    
    if (closest_info.t == INF) {
        return vec4(0.5);
    }
    
    vec3 light_pos = vec3(cos(iTime), 2.0, -5.0 + sin(iTime));
    
    vec3 hit_pos = r.pos + r.dir*closest_info.t;
    vec3 light_dir = normalize(light_pos - hit_pos);
    closest_info.color *= max(dot(closest_info.normal, light_dir), 0.0);
    
    Ray light_ray = Ray(hit_pos + light_dir*EPS, light_dir);
    HitInfo light_info;
    light_info.t = INF;
    
    if (shootRay(light_ray, light_info)) {
        return vec4(0.0);
    }
    
    return vec4(closest_info.color, 1.0);
}

void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    vec4 color = vec4(0.0);
    for (int x = 0; x < SAMPLE; x++) {
        for (int y = 0; y < SAMPLE; y++) {
            vec2 uv = (fragCoord + OFF2 + OFF1*vec2(x, y))/iResolution.xy;
            uv -= 0.5;
            uv.x *= iResolution.x / iResolution.y;
            uv *= 2.0 * tan(FOV * PI / 360.0);

            vec3 rayDir = normalize(vec3(uv.x, uv.y, -1.0));

            color += traceRay(Ray(vec3(0.0), rayDir));
        }
    }
    
    fragColor = color / float(SAMPLE * SAMPLE);
}
