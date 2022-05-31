const $wnd = window;
/**
 * A helper method for Spring Security based form login.
 * @param username
 * @param password
 * @param options defines additional options, e.g, the loginProcessingUrl etc.
 */
export async function login(username, password, options) {
    try {
        const data = new FormData();
        data.append('username', username);
        data.append('password', password);
        const loginProcessingUrl = options && options.loginProcessingUrl ? options.loginProcessingUrl : 'login';
        const headers = getSpringCsrfTokenHeadersFromDocument(document);
        headers.source = 'typescript';
        const response = await fetch(loginProcessingUrl, {
            method: 'POST',
            body: data,
            headers
        });
        // This code assumes that a VaadinSavedRequestAwareAuthenticationSuccessHandler is used on the server side,
        // setting these header values based on the "source=typescript" header set above
        const result = response.headers.get('Result');
        const savedUrl = response.headers.get('Saved-url') || undefined;
        const defaultUrl = response.headers.get('Default-url') || undefined;
        const loginSuccessful = response.ok && result === 'success';
        if (loginSuccessful) {
            const vaadinCsrfToken = response.headers.get('Vaadin-CSRF') || undefined;
            updateVaadinCsrfToken(vaadinCsrfToken);
            const springCsrfHeader = response.headers.get('Spring-CSRF-header') || undefined;
            const springCsrfToken = response.headers.get('Spring-CSRF-token') || undefined;
            if (springCsrfHeader && springCsrfToken) {
                const springCsrfTokenInfo = {};
                springCsrfTokenInfo._csrf = springCsrfToken;
                springCsrfTokenInfo._csrf_header = springCsrfHeader;
                updateSpringCsrfMetaTags(springCsrfTokenInfo);
            }
            return {
                error: false,
                token: vaadinCsrfToken,
                redirectUrl: savedUrl,
                defaultUrl
            };
        }
        else {
            return {
                error: true,
                errorTitle: 'Incorrect username or password.',
                errorMessage: 'Check that you have entered the correct username and password and try again.'
            };
        }
    }
    catch (e) {
        return {
            error: true,
            errorTitle: e.name,
            errorMessage: e.message
        };
    }
}
/**
 * A helper method for Spring Security based form logout
 * @param options defines additional options, e.g, the logoutUrl.
 */
export async function logout(options) {
    var _a, _b;
    // this assumes the default Spring Security logout configuration (handler URL)
    const logoutUrl = options && options.logoutUrl ? options.logoutUrl : 'logout';
    try {
        const headers = getSpringCsrfTokenHeadersFromDocument(document);
        await doLogout(logoutUrl, headers);
    }
    catch {
        try {
            const response = await fetch('?nocache');
            const responseText = await response.text();
            const doc = new DOMParser().parseFromString(responseText, 'text/html');
            const headers = getSpringCsrfTokenHeadersFromDocument(doc);
            await doLogout(logoutUrl, headers);
        }
        catch (error) {
            // clear the token if the call fails
            (_b = (_a = $wnd.Vaadin) === null || _a === void 0 ? void 0 : _a.TypeScript) === null || _b === void 0 ? true : delete _b.csrfToken;
            clearSpringCsrfMetaTags();
            throw error;
        }
    }
}
async function doLogout(logoutUrl, headers) {
    const response = await fetch(logoutUrl, { method: 'POST', headers });
    if (!response.ok) {
        throw new Error(`failed to logout with response ${response.status}`);
    }
    await updateCsrfTokensBasedOnResponse(response);
}
function updateSpringCsrfMetaTags(springCsrfInfo) {
    clearSpringCsrfMetaTags();
    const headerNameMeta = document.createElement('meta');
    headerNameMeta.name = '_csrf_header';
    headerNameMeta.content = springCsrfInfo._csrf_header;
    document.head.appendChild(headerNameMeta);
    const tokenMeta = document.createElement('meta');
    tokenMeta.name = '_csrf';
    tokenMeta.content = springCsrfInfo._csrf;
    document.head.appendChild(tokenMeta);
}
function clearSpringCsrfMetaTags() {
    Array.from(document.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"]')).forEach((el) => el.remove());
}
const getVaadinCsrfTokenFromResponseBody = (body) => {
    const match = body.match(/window\.Vaadin = \{TypeScript: \{"csrfToken":"([0-9a-zA-Z\\-]{36})"}};/i);
    return match ? match[1] : undefined;
};
const getSpringCsrfTokenHeadersFromDocument = (doc) => {
    const csrfInfo = getSpringCsrfInfoFromDocument(doc);
    const headers = {};
    if (csrfInfo._csrf && csrfInfo._csrf_header) {
        headers[csrfInfo._csrf_header] = csrfInfo._csrf;
    }
    return headers;
};
const getSpringCsrfInfoFromDocument = (doc) => {
    const csrf = doc.head.querySelector('meta[name="_csrf"]');
    const csrfHeader = doc.head.querySelector('meta[name="_csrf_header"]');
    const headers = {};
    if (csrf !== null && csrfHeader !== null) {
        headers._csrf = csrf.content;
        headers._csrf_header = csrfHeader.content;
    }
    return headers;
};
const getSpringCsrfTokenFromResponseBody = (body) => {
    const doc = new DOMParser().parseFromString(body, 'text/html');
    return getSpringCsrfInfoFromDocument(doc);
};
async function updateCsrfTokensBasedOnResponse(response) {
    const responseText = await response.text();
    const token = getVaadinCsrfTokenFromResponseBody(responseText);
    updateVaadinCsrfToken(token);
    const springCsrfTokenInfo = getSpringCsrfTokenFromResponseBody(responseText);
    updateSpringCsrfMetaTags(springCsrfTokenInfo);
    return token;
}
function updateVaadinCsrfToken(token) {
    $wnd.Vaadin.TypeScript = $wnd.Vaadin.TypeScript || {};
    $wnd.Vaadin.TypeScript.csrfToken = token;
}
/**
 * A helper class for handling invalid sessions during an endpoint call.
 * E.g., you can use this to show user a login page when the session has expired.
 */
export class InvalidSessionMiddleware {
    constructor(onInvalidSessionCallback) {
        this.onInvalidSessionCallback = onInvalidSessionCallback;
    }
    async invoke(context, next) {
        const clonedContext = { ...context };
        clonedContext.request = context.request.clone();
        const response = await next(context);
        if (response.status === 401) {
            const loginResult = await this.onInvalidSessionCallback();
            if (loginResult.token) {
                clonedContext.request.headers.set('X-CSRF-Token', loginResult.token);
                return next(clonedContext);
            }
        }
        return response;
    }
}
//# sourceMappingURL=Authentication.js.map