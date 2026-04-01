
const BASE_URL = '/api';

interface RequestOptions extends RequestInit {
    params?: Record<string, string>;
    skipProxy?: boolean;
}

/**
 * 获取认证 Token
 * 优先获取代理 token (proxyToken)，如果不存在则获取普通 token
 * 这样在代理模式下，所有 API 请求都会自动使用被代理人的身份
 */
const getAuthToken = (): string | null => {
    const proxyToken = localStorage.getItem('proxyToken');
    const normalToken = localStorage.getItem('token');
    return proxyToken || normalToken;
};

async function request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
    const { params, skipProxy, ...customConfig } = options;

    let url = `${BASE_URL}${endpoint}`;
    if (params) {
        const searchParams = new URLSearchParams(params);
        url += `?${searchParams.toString()}`;
    }

    // skipProxy 为 true 时，强制使用普通 token（用于不在代理白名单中的接口）
    const token = skipProxy ? localStorage.getItem('token') : getAuthToken();

    const headers = {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers,
    };

    const config: RequestInit = {
        method: 'GET',
        ...customConfig,
        headers,
    };

    try {
        const response = await fetch(url, config);

        let data: any;
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            data = await response.json();
        } else {
            const text = await response.text();
            // If response is not OK, use text as error message
            if (!response.ok) {
                throw new Error(text || response.statusText);
            }
            // If OK but not JSON, this is unexpected for our API
            console.warn('Received non-JSON response:', text);
            throw new Error('服务器响应格式错误');
        }

        if (response.ok) {
            // Assume standard API response structure: { code: 200, data: ..., msg: ... }
            if (data && data.code === 200) {
                return data.data;
            } else {
                console.error('API Error Response:', data); // Add logging
                throw new Error(data?.msg || `API Error: ${JSON.stringify(data)}`); // Improve error message
            }
        } else {
            throw new Error(data?.msg || response.statusText);
        }
    } catch (error) {
        console.error('Request failed:', error);
        throw error;
    }
}

export const get = <T>(endpoint: string, params?: Record<string, string>, skipProxy?: boolean) =>
    request<T>(endpoint, { method: 'GET', params, skipProxy });

export const post = <T>(endpoint: string, body: any, skipProxy?: boolean) =>
    request<T>(endpoint, { method: 'POST', body: JSON.stringify(body), skipProxy });

export const upload = <T>(endpoint: string, file: File, params?: Record<string, string>) => {
    const formData = new FormData();
    formData.append('file', file);

    let url = `${BASE_URL}${endpoint}`;
    if (params) {
        const searchParams = new URLSearchParams(params);
        url += `?${searchParams.toString()}`;
    }

    const token = getAuthToken();

    return fetch(url, {
        method: 'POST',
        headers: {
            ...(token && { Authorization: `Bearer ${token}` }),
        },
        body: formData
    }).then(async res => {
        console.log('Upload Response Status:', res.status);

        const contentType = res.headers.get("content-type");
        console.log('Upload Response Content-Type:', contentType);

        if (contentType && contentType.includes("application/json")) {
            const data = await res.json();
            console.log('Upload Response Data:', data);

            if (data.code === 200) return data.data;
            throw new Error(data.msg || 'Upload Failed');
        } else {
            const text = await res.text();
            console.error('Upload Failed Non-JSON:', text);
            throw new Error(!res.ok ? (text || res.statusText) : 'Upload Failed: Invalid Response');
        }
    });
}
