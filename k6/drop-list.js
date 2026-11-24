// 앱은 perf 프로필로, 돌릴 때마다 새로 띄워서 잴 것
// docker run --rm -i grafana/k6:2.4.0 run - < k6/drop-list.js
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export const options = {
    scenarios: {
        // 워밍업
        warmup: {
            executor: 'constant-vus',
            vus: 100,
            duration: '30s',
            gracefulStop: '5s',
        },
        // 본 측정
        measure: {
            executor: 'constant-vus',
            vus: 100,
            duration: '1m',
            startTime: '35s',
        },
    },
    thresholds: {
        'http_req_duration{scenario:measure}': ['p(95)<200'],
        'http_req_failed{scenario:measure}': ['rate<0.01'],
        'http_reqs{scenario:measure}': ['count>0'],
    },
    summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
    const res = http.get(`${BASE_URL}/drops`);
    check(res, { 'status is 200': (r) => r.status === 200 });
}
