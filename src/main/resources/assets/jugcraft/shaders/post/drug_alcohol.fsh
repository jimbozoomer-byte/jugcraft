#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
layout(std140) uniform JugcraftDrugVisuals {
    vec4 Effect; // strength, blur, motion, double vision
    vec4 Look;   // time in seconds, warmth, vignette, reserved
    vec4 Clarity; // focus, energy, warm/cool tone, fatigue
    vec4 Warning; // strain severity, jitters, reserved, reserved
    vec4 Calm; // THC, opioid softness, Echo View, reserved
    vec4 Dream; // organic sight, crystalline resonance, Pattern Sight, Deep Reverie
    vec4 TrailA; // two recent directional afterimage offsets
    vec4 TrailB; // oldest offset, Clockwork glimpse strength, reserved
};
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

vec3 sampleWorld(vec2 uv) {
    vec2 halfPixel = 0.5 / vec2(textureSize(InSampler, 0));
    return texture(InSampler, clamp(uv, halfPixel, vec2(1.0) - halfPixel)).rgb;
}
float lineArt(float distance, float width) {
    return 1.0 - smoothstep(width, width + 0.0018, abs(distance));
}
float segment(vec2 p, vec2 a, vec2 b) {
    vec2 v = b - a;
    return length(p - a - v * clamp(dot(p - a, v) / dot(v, v), 0.0, 1.0));
}
float clockGear(vec2 p, float phase) {
    float r = length(p);
    float a = r > 0.0001 ? atan(p.y, p.x) : 0.0;
    float teeth = lineArt(r - (0.052 + 0.006 * cos(a * 12.0 + phase)), 0.0014);
    float hub = lineArt(r - 0.019, 0.0014);
    float spokes = lineArt(sin(a * 4.0 + phase * 0.333), 0.06) * smoothstep(0.019, 0.022, r) * (1.0 - smoothstep(0.045, 0.05, r));
    return max(teeth, max(hub, spokes));
}
float clockFigure(vec2 p) {
    // Schematic pointed head, ears, body and limbs: a glimpse, never a world entity.
    float head = lineArt(abs(p.x) + abs(p.y - 0.032) - 0.018, 0.0012);
    float ears = min(segment(p, vec2(-0.015, 0.032), vec2(-0.03, 0.045)), segment(p, vec2(0.015, 0.032), vec2(0.03, 0.045)));
    float body = segment(p, vec2(0.0, 0.014), vec2(0.0, -0.035));
    float arms = min(segment(p, vec2(-0.032, -0.009), vec2(0.0, 0.002)), segment(p, vec2(0.0, 0.002), vec2(0.032, -0.009)));
    float legs = min(segment(p, vec2(-0.022, -0.063), vec2(0.0, -0.035)), segment(p, vec2(0.0, -0.035), vec2(0.022, -0.063)));
    return max(head, 1.0 - smoothstep(0.0012, 0.003, min(min(ears, body), min(arms, legs))));
}
void main() {
    vec2 centered = texCoord - 0.5;
    // Leave the target at the center clear. Distortion grows smoothly into the periphery.
    float edge = smoothstep(0.12, 0.62, length(centered));
    float strength = clamp(Effect.x, 0.0, 1.0);
    float angle = sin(Look.x * 0.73) * 0.014;
    mat2 turn = mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
    vec2 sway = (turn * centered - centered + vec2(sin(Look.x * 0.91) * 0.004, cos(Look.x * 0.67) * 0.002)) * edge;
    vec2 uv = texCoord + sway * strength * Effect.z;
    // Jitters stay at the edges and never move the camera or aiming ray.
    uv += vec2(sin(Look.x * 9.1), sin(Look.x * 7.7 + 1.3)) * 0.0012 * edge * Warning.y * Effect.z;
    uv += centered * sin(Look.x * 0.52) * 0.004 * Calm.z * Effect.z * edge;
    vec2 organic = vec2(sin(texCoord.y * 10.0 + Look.x * 0.63), cos(texCoord.x * 9.0 - Look.x * 0.51));
    uv += organic * 0.0035 * Dream.x * Effect.z * edge;
    vec3 color = sampleWorld(uv);
    if (max(Dream.z, Dream.w) > 0.0 && Effect.z > 0.0 && Effect.w > 0.0) {
        float movement = length(TrailA.xy) + length(TrailA.zw) + length(TrailB.xy);
        if (movement > 0.00001) {
            vec3 afterimage = sampleWorld(uv + TrailA.xy) * 0.5 + sampleWorld(uv + TrailA.zw) * 0.3 + sampleWorld(uv + TrailB.xy) * 0.2;
            color = mix(color, afterimage, edge * max(Dream.z * 0.25, Dream.w * 0.30) * Effect.z * Effect.w);
        }
    }
    float haze = max(max(strength, Warning.x * 0.60), max(Calm.y * 0.65, Calm.x * 0.20));
    vec2 radius = vec2(0.0030, 0.0045) * edge * haze * Effect.y;
    if (Effect.y > 0.0) {
        vec3 soft = (sampleWorld(uv + vec2(radius.x, 0)) + sampleWorld(uv - vec2(radius.x, 0))
                   + sampleWorld(uv + vec2(0, radius.y)) + sampleWorld(uv - vec2(0, radius.y))) * 0.25;
        color = mix(color, soft, min(0.70, edge * haze * Effect.y));
    }
    if (Effect.w > 0.0) {
        // Symmetric ghosting, no previous-frame history or unbounded trails.
        vec2 ghost = vec2(0.012, 0.003) * strength * Effect.w * edge;
        vec3 doubled = (sampleWorld(uv + ghost) + sampleWorld(uv - ghost)) * 0.5;
        color = mix(color, doubled, min(0.48, edge * strength * Effect.w));
    }
    color *= mix(vec3(1.0), vec3(1.0, 0.97, 0.90), strength * Look.y * 8.0);
    color *= 1.0 - edge * edge * strength * Look.z;
    // Restrained contrast and tone: no hidden information, night-vision gain or FOV changes.
    color = clamp((color - 0.5) * (1.0 + Clarity.x * 0.12) + 0.5, 0.0, 1.0);
    color *= vec3(1.0 + Clarity.z * 0.018, 1.0, 1.0 - Clarity.z * 0.025);
    color *= 1.0 - edge * edge * Clarity.y * 0.22;
    float grey = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(color, vec3(grey), Clarity.w * 0.35);
    // Warm botanical color, quiet opioid edges, and a cool detached scouting view.
    vec3 botanical = mix(vec3(grey), color, 1.0 + Calm.x * 0.10);
    color = mix(color, botanical * vec3(1.025, 1.012, 0.975), Calm.x * 0.45);
    color = mix(color, vec3(grey) * vec3(0.98, 1.0, 1.025), Calm.y * 0.20);
    color *= 1.0 - edge * edge * Calm.y * 0.18;
    if (Calm.z > 0.0) {
        vec2 separation = vec2(0.003, 0.001) * Calm.z * Effect.w * edge;
        vec3 echoColor = vec3(sampleWorld(uv + separation).r, color.g, sampleWorld(uv - separation).b);
        color = mix(color, echoColor, Calm.z * 0.28);
        color = mix(color, vec3(grey) * vec3(0.88, 1.025, 1.12), Calm.z * 0.22);
        color = mix(color, vec3(0.28, 0.60, 0.66), edge * edge * Calm.z * 0.08);
    }
    // Organic violet warmth and a restrained six-fold peripheral crystal motif.
    color *= vec3(1.0 + Dream.x * 0.028, 1.0 - Dream.x * 0.012, 1.0 + Dream.x * 0.038);
    float crystalRadius = length(centered);
    float crystalAngle = crystalRadius > 0.0001 ? atan(centered.y, centered.x) : 0.0;
    float facet = 0.78 + 0.12 * cos(crystalAngle * 6.0);
    float crystal = 1.0 - smoothstep(0.004, 0.012, abs(crystalRadius - facet * 0.58));
    float resonancePulse = mix(1.0, 0.78 + 0.22 * sin(Look.x * 0.85), Effect.z);
    color = mix(color, vec3(0.48, 0.80, 0.67), crystal * edge * Dream.y * 0.13 * resonancePulse);
    color *= vec3(1.0 + Dream.y * 0.012, 1.0 + Dream.y * 0.022, 1.0 - Dream.y * 0.012);
    if (max(Dream.z, Dream.w) > 0.0) {
        vec2 aspect = vec2(float(textureSize(InSampler, 0).x) / float(textureSize(InSampler, 0).y), 1.0);
        vec2 p = centered * aspect;
        float r = length(p);
        float a = r > 0.0001 ? atan(p.y, p.x) : 0.0;
        float drift = sin(Look.x * 0.23) * 0.10 * Effect.z;
        // Six-fold lattice for Pattern Sight; eight-fold nested geometry for Deep Reverie.
        float hexagon = abs(cos(a * 3.0 + drift));
        float lattice = lineArt(fract((r + hexagon * 0.055) * 9.0) - 0.5, 0.035);
        float star = lineArt(r - (0.36 + 0.055 * cos(a * 8.0 - drift)), 0.0025);
        float innerStar = lineArt(r - (0.26 + 0.035 * cos(a * 8.0 + drift)), 0.002);
        float deepLattice = lineArt(fract((r + cos(a * 4.0) * 0.035) * 12.0) - 0.5, 0.025);
        color = mix(color, vec3(0.78, 0.46, 0.82), lattice * edge * Dream.z * 0.20);
        color = mix(color, vec3(0.48, 0.80, 0.86), max(max(star, innerStar), deepLattice * 0.45) * edge * Dream.w * 0.30);
        color *= vec3(1.0 + Dream.z * 0.025, 1.0 + Dream.w * 0.018, 1.0 + max(Dream.z, Dream.w) * 0.03);
        if (Dream.w > 0.0) {
            float mechanism = 0.0;
            float figures = 0.0;
            for (int side = 0; side < 2; side++) {
                vec2 portal = vec2(side == 0 ? -0.37 : 0.37, 0.01) * aspect;
                vec2 q = p - portal;
                float arch = lineArt(length(q * vec2(0.66, 1.0)) - 0.17, 0.002);
                float gear = clockGear(q - vec2(0.0, 0.095), Look.x * 0.18 * Effect.z);
                mechanism = max(mechanism, max(arch * 0.65, gear));
                figures = max(figures, clockFigure(q + vec2(0.0, 0.018)));
            }
            color = mix(color, vec3(0.84, 0.66, 0.36), mechanism * edge * TrailB.z * 0.30);
            color = mix(color, vec3(0.62, 0.86, 0.82), figures * edge * TrailB.z * 0.34);
        }
    }
    // Four-second peripheral breathing becomes a static rim with Reduced Motion / sway at zero.
    float breath = mix(1.0, 0.86 + 0.14 * sin(Look.x * 1.57), Effect.z);
    vec3 warningColor = mix(vec3(0.76, 0.43, 0.10), vec3(0.65, 0.08, 0.055), Warning.x);
    color = mix(color, warningColor, edge * edge * Warning.x * 0.22 * breath);
    fragColor = vec4(color, 1.0);
}
