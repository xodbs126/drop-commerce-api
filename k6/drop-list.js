// 앱은 perf 프로필로, 돌릴 때마다 새로 띄워서 잴 것
// 조건은 QUERY로 고름 (기본 default)
// docker run --rm -i -e QUERY=ended-deep grafana/k6:2.4.0 run - < k6/drop-list.js
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

// 행 수는 더미 6만 기준
const QUERIES = {
    'default': '/drops',                                // 1,468행
    'ongoing': '/drops?phase=ONGOING',                  // 162행
    'ended-first': '/drops?phase=ENDED',                // 56,415행, 첫 페이지
    'ended-deep': '/drops?phase=ENDED&page=2000',       // 같은 조건, offset 40,000 (마지막은 2820)
    'keyword': `/drops?keyword=${encodeURIComponent('한정')}`, // 74행, LIKE '%한정%'
};

const QUERY = __ENV.QUERY || 'default';
const PATH = QUERIES[QUERY];

if (!PATH) {
    throw new Error(`QUERY는 ${Object.keys(QUERIES).join(', ')} 중 하나: ${QUERY}`);
}

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
        // 빈 목록을 재지 않게 (더미를 안 넣었거나 페이지가 범위를 넘으면 실패)
        'checks{scenario:measure}': ['rate==1'],
    },
    summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export function setup() {
    console.log(`QUERY=${QUERY} ${BASE_URL}${PATH}`);
}

export default function () {
    const res = http.get(`${BASE_URL}${PATH}`);
    check(res, {
        'status is 200': (r) => r.status === 200,
        'content is not empty': (r) => r.status === 200 && r.json('content').length > 0,
    });
}
