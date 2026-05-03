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
const float COLOR_INTENSITY = 0.45;
const float COLOR_BLEND_WIDTH = 0.08;
const float SELECTOR_COVER = 0.12;
const float SELECTOR_BRIGHTNESS = 0.88;
const float SELECTOR_DETAIL_STRENGTH = 0.10;
const float COVERAGE_COVER = 0.42;
const float COVERAGE_BRIGHTNESS = 0.72;
const vec2 PATTERN_DRIFT = vec2(-0.018, 0.014);
const vec2 DETAIL_DRIFT = vec2(-0.010, 0.008);

// 简化的 Simplex 风格噪声函数。
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

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);

    // 缩放 UV 用于噪声采样。
    vec2 uv = texCoord * 1.8;
    float aspect = OutSize.x / OutSize.y;
    uv.x *= aspect;
    float scaledTime = Time * TIME_SCALE;
    vec2 patternUv = uv + scaledTime * PATTERN_DRIFT;
    vec2 detailUv = uv + scaledTime * DETAIL_DRIFT;

    // 归一化权重：每种象征获得的全屏面积比例近似等于自身权重占比。
    float weightTotal = Weight0 + Weight1 + Weight2 + Weight3 + Weight4 + Weight5;
    float useFallback = 1.0 - step(0.001, weightTotal);
    weightTotal = max(weightTotal, 0.001);
    float w0 = mix(Weight0 / weightTotal, 1.0 / 6.0, useFallback);
    float w1 = mix(Weight1 / weightTotal, 1.0 / 6.0, useFallback);
    float w2 = mix(Weight2 / weightTotal, 1.0 / 6.0, useFallback);
    float w3 = mix(Weight3 / weightTotal, 1.0 / 6.0, useFallback);
    float w4 = mix(Weight4 / weightTotal, 1.0 / 6.0, useFallback);
    float w5 = mix(Weight5 / weightTotal, 1.0 / 6.0, useFallback);

    // 使用累计权重把 0..1 的选择噪声切分为 6 个面积区间。
    float c0 = w0;
    float c1 = c0 + w1;
    float c2 = c1 + w2;
    float c3 = c2 + w3;
    float c4 = c3 + w4;

    // 主选择噪声决定每个像素属于哪个象征，参考云层 smoothstep 拉开柔和过渡。
    float selectorBase = fbm(patternUv * 1.25 + vec2(10.0, 10.0));
    float selectorDetail = fbm(detailUv * 2.7 + vec2(4.3, 7.1));
    float selector = clamp(selectorBase + (selectorDetail - 0.5) * SELECTOR_DETAIL_STRENGTH, 0.0, 1.0);
    selector = smoothstep(SELECTOR_COVER, SELECTOR_BRIGHTNESS, selector);

    // 累计边界附近做软过渡；零权重颜色不参与面积分配。
    float blendWidth = COLOR_BLEND_WIDTH * mix(0.75, 1.35, selectorDetail);
    float e0 = smoothstep(c0 - blendWidth, c0 + blendWidth, selector);
    float e1 = smoothstep(c1 - blendWidth, c1 + blendWidth, selector);
    float e2 = smoothstep(c2 - blendWidth, c2 + blendWidth, selector);
    float e3 = smoothstep(c3 - blendWidth, c3 + blendWidth, selector);
    float e4 = smoothstep(c4 - blendWidth, c4 + blendWidth, selector);

    float m0 = (1.0 - e0) * step(0.001, w0);
    float m1 = e0 * (1.0 - e1) * step(0.001, w1);
    float m2 = e1 * (1.0 - e2) * step(0.001, w2);
    float m3 = e2 * (1.0 - e3) * step(0.001, w3);
    float m4 = e3 * (1.0 - e4) * step(0.001, w4);
    float m5 = e4 * step(0.001, w5);

    float maskTotal = max(m0 + m1 + m2 + m3 + m4 + m5, 0.001);
    vec3 blended = (Color0 * m0 + Color1 * m1 + Color2 * m2 + Color3 * m3 + Color4 * m4 + Color5 * m5) / maskTotal;

    // 覆盖遮罩决定哪里显色、哪里透明；显色区域内部再按 selector 分配象征颜色。
    float coverageNoise = fbm(patternUv * 1.15 + vec2(13.7, 2.4));
    float coverageDetail = fbm(detailUv * 2.4 + vec2(1.9, 11.6));
    float coverageMask = clamp(coverageNoise + (coverageDetail - 0.5) * 0.12, 0.0, 1.0);
    coverageMask = smoothstep(COVERAGE_COVER, COVERAGE_BRIGHTNESS, coverageMask);

    // 边缘渐隐，避免画面边缘过亮。
    vec2 vig = texCoord * 2.0 - 1.0;
    float vignette = 1.0 - smoothstep(0.3, 1.3, length(vig));

    // 加色叠加：只增强颜色，不压暗原画面。
    float strength = COLOR_INTENSITY * coverageMask * vignette;
    vec3 result = min(original.rgb + blended * strength, 1.0);

    fragColor = vec4(result, 1.0);
}
