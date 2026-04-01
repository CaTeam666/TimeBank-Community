import { auth } from './auth';

export const setupFetchInterceptor = () => {
    const originalFetch = window.fetch;

    window.fetch = async (...args) => {
        let [resource, config] = args;
        
        let url = '';
        if (typeof resource === 'string') {
            url = resource;
        } else if (resource instanceof URL) {
            url = resource.toString();
        } else if (resource && typeof resource === 'object' && 'url' in resource) {
            url = (resource as any).url;
        }

        // 只有向 /api 开头的请求才注入 Token
        if (url.includes('/api/')) {
            const token = auth.getToken();
            if (token) {
                config = config || {};
                const headers = new Headers(config.headers || {});
                // 接口要求直接传 Token 值，不加 Bearer
                headers.set('Authorization', token);
                config.headers = headers;
            }
        }

        const response = await originalFetch(resource, config);

        // 如果 HTTP status 是 401
        if (response.status === 401) {
            auth.clear();
            if (window.location.hash !== '#/login') {
                window.location.hash = '#/login';
            }
            return response;
        }

        // 检查返回体是否为 JSON 且业务 code 为 401
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const clone = response.clone();
            try {
                const data = await clone.json();
                if (data && data.code === 401) {
                    auth.clear();
                    if (window.location.hash !== '#/login') {
                        window.location.hash = '#/login';
                    }
                }
            } catch (e) {
                // 解析 JSON 失败，可以忽略
            }
        }

        return response;
    };
};
