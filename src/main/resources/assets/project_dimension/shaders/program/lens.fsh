#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
uniform vec2 OutSize;

uniform vec3 Color0; uniform vec3 Color1;
uniform vec3 Color2; uniform vec3 Color3;
uniform vec3 Color4; uniform vec3 Color5;

uniform float Weight0; uniform float Weight1;
uniform float Weight2; uniform float Weight3;
uniform float Weight4; uniform float Weight5;

uniform float Time;
out vec4 fragColor;

const float TIME_SCALE = 0.333333;
const float CLOUD_INTENSITY = 1.02;
const float CLOUD_ZOOM = 1.25;
const float CLOUD_COVER = 0.22;
const float CLOUD_BRIGHTNESS = 0.70;
const float DETAIL_STRENGTH = 0.30;
const vec2 PATTERN_DRIFT = vec2(-0.018, 0.014);
const vec2 DETAIL_DRIFT = vec2(-0.010, 0.008);

// 基于 simplex 风格噪声构造维度云层。
vec2 hash(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise(vec2 p) {
    const float K1 = 0.366025404;
    const float K2 = 0.211324865;
    vec2 i = floor(p + (p.x + p.y) * K1);
    vec2 a = p - i + (i.x + i.y) * K2;
    float m = step(a.y, a.x);
    vec2 o = vec2(m, 1.0 - m);
    vec2 b = a - o + K2;
    vec2 c = a - 1.0 + 2.0 * K2;
    vec3 h = max(0.5 - vec3(dot(a, a), dot(b, b), dot(c, c)), 0.0);
    vec3 n = h * h * h * h * vec3(dot(a, hash(i)), dot(b, hash(i + o)), dot(c, hash(i + 1.0)));
    return dot(n, vec3(70.0));
}

const mat2 rot = mat2(1.6, 1.2, -1.2, 1.6);

float fbm(vec2 p) {
    float amp = 0.5;
    float h = 0.0;
    for (int i = 0; i < 4; i++) {
        h += amp * noise(p);
        amp *= 0.5;
        p = rot * p;
    }
    return 0.5 + 0.5 * h;
}

float symbolCloud(vec2 uv, vec2 seed, vec2 drift, float weight, float phase) {
    if (weight <= 0.001) {
        return 0.0;
    }

    float time = Time * TIME_SCALE;
    vec2 mainUv = uv * mix(0.92, 1.28, weight) + seed + drift * time;
    vec2 detailUv = uv * mix(2.10, 2.85, weight) + seed * 1.7 - drift.yx * time * 0.7 + phase;

    float body = fbm(mainUv * CLOUD_ZOOM);
    float detail = fbm(detailUv);
    float wisps = fbm(mainUv * 0.65 + vec2(phase, -phase));

    float density = body + (detail - 0.5) * DETAIL_STRENGTH + (wisps - 0.5) * 0.24;
    float cover = mix(0.42, 0.24, weight);
    float brightness = mix(0.64, 0.82, weight);
    float mask = smoothstep(cover, brightness, density);
    return mask * mix(0.60, 1.30, weight);
}

vec3 screenBlend(vec3 base, vec3 layer) {
    return 1.0 - (1.0 - base) * (1.0 - layer);
}

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);

    vec2 uv = texCoord - 0.5;
    uv.x *= OutSize.x / OutSize.y;
    uv *= 1.85;

    float globalTime = Time * TIME_SCALE;
    vec2 cloudUv = uv + PATTERN_DRIFT * globalTime;
    vec2 detailUv = uv + DETAIL_DRIFT * globalTime;

    // 全局云层覆盖，决定哪里显现“维度透镜”效果。
    float coverageBase = fbm(cloudUv * CLOUD_ZOOM + vec2(10.0, 10.0));
    float coverageDetail = fbm(detailUv * 2.35 + vec2(4.3, 7.1));
    float coverageNoise = clamp(coverageBase + (coverageDetail - 0.5) * 0.16, 0.0, 1.0);
    float coverageMask = smoothstep(CLOUD_COVER, CLOUD_BRIGHTNESS, coverageNoise);

    // 六个基础象征各自形成一层流动彩云，颜色由象征注解颜色决定。
    float c0 = symbolCloud(cloudUv, vec2(1.7, 8.2), vec2(-0.020, 0.013), Weight0, 0.13);
    float c1 = symbolCloud(cloudUv, vec2(8.4, 2.1), vec2(-0.014, 0.009), Weight1, 0.31);
    float c2 = symbolCloud(cloudUv, vec2(4.6, 11.3), vec2(-0.011, -0.010), Weight2, 0.57);
    float c3 = symbolCloud(cloudUv, vec2(12.2, 5.9), vec2(-0.024, 0.006), Weight3, 0.79);
    float c4 = symbolCloud(cloudUv, vec2(6.3, 14.7), vec2(-0.009, 0.015), Weight4, 1.03);
    float c5 = symbolCloud(cloudUv, vec2(15.5, 9.8), vec2(-0.017, -0.005), Weight5, 1.27);

    float cloudTotal = c0 + c1 + c2 + c3 + c4 + c5;
    vec3 tint = vec3(0.0);
    if (cloudTotal > 0.001) {
        tint = (
                Color0 * c0 +
                Color1 * c1 +
                Color2 * c2 +
                Color3 * c3 +
                Color4 * c4 +
                Color5 * c5
        ) / cloudTotal;
    }

    // 再叠一层细节权重，让高权重象征更容易成为主导色，但不会整屏霸占。
    float weightPresence = clamp(cloudTotal * 0.42, 0.0, 1.0);
    float innerGlow = smoothstep(0.28, 0.88, coverageDetail);

    vec2 vig = texCoord * 2.0 - 1.0;
    float vignette = 1.0 - smoothstep(0.35, 1.25, length(vig));

    float strength = CLOUD_INTENSITY * coverageMask * mix(0.88, 1.18, innerGlow) * mix(0.95, 1.28, weightPresence) * mix(0.94, 1.02, vignette);
    vec3 cloudLayer = tint * strength;
    vec3 result = screenBlend(original.rgb, cloudLayer);

    fragColor = vec4(result, 1.0);
}
