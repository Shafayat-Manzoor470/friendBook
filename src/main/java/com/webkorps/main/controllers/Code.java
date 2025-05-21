//<!DOCTYPE html>
//<html lang="en">
//<head>
//    <meta charset="UTF-8" />
//    <title>Friend-Book | Home</title>
//    <meta name="viewport" content="width=device-width, initial-scale=1" />
//    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
//    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet" />
//    <style>
//        body {
//            background-color: #f0f2f5;
//            font-family: 'Inter', sans-serif;
//        }
//        .container {
//            margin-top: 30px;
//            padding: 0 15px;
//        }
//        .card {
//            border-radius: 12px;
//            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
//            border: 1px solid #e0e0e0;
//            background: white;
//            margin-bottom: 20px;
//        }
//        .card-body {
//            padding: 20px;
//        }
//        .post-image {
//            width: 100%;
//            height: auto;
//            object-fit: contain;
//            max-height: 400px;
//            border-radius: 10px;
//        }
//        .btn-like, .btn-comment {
//            background: transparent;
//            border: none;
//            font-size: 14px;
//            padding: 8px 16px;
//            border-radius: 8px;
//            margin-right: 10px;
//            color: #6c757d;
//            transition: 0.3s;
//            cursor: pointer;
//            user-select: none;
//        }
//        .btn-like:hover {
//            background-color: #e0f7fa;
//            color: #007bff;
//        }
//        .btn-comment:hover {
//            background-color: #e8f5e9;
//            color: #28a745;
//        }
//        .btn-like.active {
//            color: #007bff;
//            font-weight: bold;
//        }
//        .btn-comment.active {
//            color: #28a745;
//            font-weight: bold;
//        }
//        .notification-btn {
//            background-color: #ff4757;
//            color: white;
//            padding: 8px 16px;
//            border-radius: 20px;
//            border: none;
//            cursor: pointer;
//            user-select: none;
//        }
//        .notification-btn:hover {
//            background-color: #c62828;
//        }
//        .post-upload-box {
//            background: #fff;
//            padding: 20px;
//            border-radius: 12px;
//            margin-bottom: 30px;
//            border: 1px solid #e0e0e0;
//        }
//        #notifications, #searchResult {
//            margin-bottom: 30px;
//        }
//        .send-request-btn.disabled {
//            background-color: #b0bec5;
//            cursor: not-allowed;
//        }
//        .post-time {
//            font-size: 12px;
//            color: #9e9e9e;
//            display: block;
//            margin-bottom: 5px;
//        }
//        #comment-box- input, #comment-box- textarea {
//            max-width: 100%;
//        }
//        #comment-box input[type="text"] {
//            margin-bottom: 8px;
//        }
//        .comments-list .border {
//            background-color: #f8f9fa;
//            border-radius: 6px;
//        }
//        .like-count {
//            font-size: 12px;
//            color: #6c757d;
//            margin-left: 5px;
//        }
//    </style>
//</head>
//<body>
//<div class="container">
//    <div class="row mb-4">
//        <div class="col-md-8">
//            <input type="text" id="searchInput" class="form-control" placeholder="Search by username..." />
//        </div>
//        <div class="col-md-4 text-end">
//            <button class="btn notification-btn" onclick="loadNotifications()">Notifications</button>
//        </div>
//    </div>
//
//    <div id="searchResult"></div>
//    <div id="notifications"></div>
//
//    <div class="post-upload-box">
//        <h5>Create a Post</h5>
//        <input type="file" id="postImage" class="form-control mb-2" accept="image/*" />
//        <button class="btn btn-success" onclick="uploadPost()">Post</button>
//    </div>
//
//    <div id="postFeed"></div>
//</div>
//
//<script>
//    // Attach JWT to all fetch requests
//    (function() {
//        const originalFetch = window.fetch;
//        window.fetch = async (input, init = {}) => {
//            const jwtToken = localStorage.getItem('jwtToken');
//            init.headers = init.headers || {};
//            if (jwtToken) init.headers['Authorization'] = `Bearer ${jwtToken}`;
//            const response = await originalFetch(input, init);
//            if (response.status === 401 || response.status === 403) {
//                alert("Session expired. Please log in again.");
//                localStorage.removeItem('jwtToken');
//                window.location.href = "/login.html";
//            }
//            return response;
//        };
//    })();
//
//    function getUserIdFromToken() {
//        const token = localStorage.getItem('jwtToken');
//        if (!token) {
//            console.warn("JWT not found.");
//            return null;
//        }
//        try {
//            const base64Url = token.split('.')[1];
//            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
//            const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
//                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
//            }).join(''));
//            const parsedPayload = JSON.parse(jsonPayload);
//            return parsedPayload.userId; // Assuming your user ID claim is 'userId'
//        } catch (error) {
//            console.error("Error decoding JWT:", error);
//            return null;
//        }
//    }
//
//    let currentUserId = getUserIdFromToken();
//    if (!currentUserId) {
//        console.warn("Could not retrieve user ID from JWT.");
//        // Optionally redirect to login if user ID is essential
//        // window.location.href = "/login.html";
//    } else {
//        loadPosts();
//    }
//
//    function searchUser() {
//        const query = document.getElementById('searchInput').value.trim();
//        if (!query) {
//            document.getElementById('searchResult').innerHTML = '';
//            return;
//        }
//        fetch(`/api/user/search/${query}`)
//            .then(res => res.json())
//            .then(users => {
//                let html = '';
//                users.forEach(user => {
//                    html += `
//                        <div class="card p-3">
//                            <div class="d-flex justify-content-between align-items-center">
//                                <span>${user.username}</span>
//                                <button class="btn btn-primary btn-sm send-request-btn" onclick="sendRequest(${user.id}, this)">Send Friend Request</button>
//                            </div>
//                        </div>`;
//                });
//                document.getElementById('searchResult').innerHTML = html;
//            })
//            .catch(error => console.error("Failed to search users:", error));
//    }
//    document.getElementById('searchInput').addEventListener('input', searchUser);
//
//    function sendRequest(userId, button) {
//        fetch(`/api/user/send-request/${userId}`, { method: 'POST' })
//            .then(res => {
//                if (!res.ok) throw new Error();
//                button.textContent = 'Request Sent';
//                button.disabled = true;
//                button.classList.add('disabled');
//            })
//            .catch(error => console.error("Failed to send request:", error));
//    }
//
//    function loadNotifications() {
//        fetch('/api/user/notifications')
//            .then(res => res.json())
//            .then(requests => {
//                let html = requests.length ? '' : '<p>No new friend requests.</p>';
//                requests.forEach(req => {
//                    html += `
//                        <div class="card p-2">
//                            <p><strong>${req.username}</strong> sent you a friend request.</p>
//                            <button class="btn btn-success btn-sm me-2" onclick="respondRequest(${req.id}, true)">Accept</button>
//                            <button class="btn btn-danger btn-sm" onclick="respondRequest(${req.id}, false)">Reject</button>
//                        </div>`;
//                });
//                document.getElementById('notifications').innerHTML = html;
//            })
//            .catch(error => console.error("Failed to load notifications:", error));
//    }
//
//    function respondRequest(userId, accept) {
//        fetch(`/api/user/respond-request/${userId}?accept=${accept}`, { method: 'POST' })
//            .then(res => res.json())
//            .then(data => {
//                loadNotifications();
//                alert(data.message);
//            })
//            .catch(error => console.error("Failed to respond to friend request:", error));
//    }
//
//    function uploadPost() {
//        const file = document.getElementById('postImage').files[0];
//        if (!file || !file.type.startsWith('image/')) {
//            alert('Select a valid image!');
//            return;
//        }
//        const formData = new FormData();
//        formData.append("image", file);
//        fetch('/api/user/upload', { method: 'POST', body: formData })
//            .then(res => {
//                if (!res.ok) throw new Error();
//                loadPosts();
//                document.getElementById('postImage').value = "";
//            })
//            .catch(error => console.error("Failed to upload post:", error));
//    }
//
//	function loadPosts() {
//	    fetch('/api/user/my-posts')
//	        .then(res => res.json())
//	        .then(posts => {
//	            let html = posts.map(post => {
//	                const likesArray = post.likes || []; // Handle potential undefined 'likes'
//	                const isLiked = likesArray.some(like => like.userId === currentUserId);
//	                const likeCount = likesArray.length;
//	                return `
//	                    <div class="card p-3 mb-4">
//	                        <strong>${post.user.username}</strong>
//	                        <span class="post-time">${new Date(post.postTime).toLocaleString()}</span>
//	                        <img src="${post.imageUrl}" class="post-image mt-2 mb-2" />
//	                        <div>
//	                            <button class="btn btn-outline-primary btn-like ${isLiked ? 'active' : ''}" data-post-id="${post.id}" onclick="toggleLike(this, ${post.id})">${isLiked ? 'Liked' : 'Like'}</button>
//	                            <span class="like-count" id="like-count-${post.id}">${likeCount}</span>
//	                            <button class="btn btn-outline-secondary btn-comment" onclick="toggleCommentBox(${post.id})">Comment</button>
//	                        </div>
//	                        <div id="comment-box-${post.id}" class="mt-3" style="display: none;">
//	                            <input type="text" class="form-control mb-2" placeholder="Write a comment..." id="comment-input-${post.id}">
//	                            <button class="btn btn-sm btn-success" onclick="submitComment(${post.id})">Post Comment</button>
//	                        </div>
//	                        <div id="comments-${post.id}" class="mt-3"></div>
//	                    </div>`;
//	            }).join('');
//	            document.getElementById('postFeed').innerHTML = html;
//	            posts.forEach(post => getComments(post.id));
//	        });
//	}
//
//    function toggleLike(button, postId) {
//        const isLiked = button.classList.contains('active');
//        const likeCountSpan = document.getElementById(`like-count-${postId}`);
//        let newLikeCount = parseInt(likeCountSpan.textContent);
//        const userIdParam = `userId=${currentUserId}`;
//        let url = `/api/user/like/${postId}?${userIdParam}`;
//        let method = 'POST';
//
//        if (isLiked) {
//            url = `/api/user/unlike/${postId}?${userIdParam}`;
//        }
//
//        fetch(url, { method: method })
//            .then(response => {
//                if (response.ok) {
//                    button.classList.toggle('active');
//                    button.textContent = button.classList.contains('active') ? 'Liked' : 'Like';
//                    likeCountSpan.textContent = button.classList.contains('active') ? newLikeCount + 1 : newLikeCount - 1;
//                } else if (response.status === 400) {
//                    alert(isLiked ? "You have not liked the post" : "Already liked the post");
//                } else if (response.status === 403) {
//                    alert("You are not authorized to perform this action.");
//                    console.error("Like/Unlike request forbidden");
//                } else {
//                    console.error("Failed to like/unlike post", response.status);
//                }
//            })
//            .catch(error => console.error("Error liking/unliking post:", error));
//    }
//
//    function toggleCommentBox(postId) {
//        const box = document.getElementById(`comment-box-${postId}`);
//        if (!box) return;
//        box.style.display = (box.style.display === "none" || box.style.display === "") ? "block" : "none";
//    }
//
//    function submitComment(postId) {
//        const input = document.getElementById(`comment-input-${postId}`);
//        const text = input.value.trim();
//        if (!text) {
//            alert("Please write a comment.");
//            return;
//        }
//        fetch(`/api/user/add/${postId}`, {
//            method: 'POST',
//            headers: {
//                'Content-Type': 'application/json',
//            },
//            body: JSON.stringify({ text })
//        })
//        .then(res => {
//            if (!res.ok) throw new Error();
//            input.value = '';
//            const commentsList = document.getElementById(`comments-${postId}`);
//            const commentDiv = document.createElement('div');
//            commentDiv.className = 'border p-2 mb-1 rounded';
//            commentDiv.innerHTML = `<strong>You</strong>: ${text}`;
//            commentsList.appendChild(commentDiv);
//        })
//        .catch(error => console.error("Failed to post comment:", error));
//    }
//
//	function getComments(postId) {
//	    fetch(`/api/user/post/${postId}`)
//	        .then(res => res.json())
//	        .then(comments => {
//	            const html = comments.map(c => `
//	                <div class="border p-2 mb-1 rounded">
//	                    <strong>${c.user.username}</strong>: ${c.content}
//	                </div>`).join('');
//	            document.getElementById(`comments-${postId}`).innerHTML = html;
//	        });
//	}
//</script>
//</body>
//</html>