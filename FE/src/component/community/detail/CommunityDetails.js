import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Cookies from 'js-cookie';
import { getCookie } from '../../util/cookieUtil';
import Comment from './../../comment/Comment';
import jwtAxios from "../../util/jwtUtil"


function CommunityDetails() {
  const [community, setCommunity] = useState(null);
  const params = useParams();
  const navigate = useNavigate();

  const userToken = Cookies.get('user');
  const parsedToken = userToken ? JSON.parse(decodeURIComponent(userToken)) : null;

  useEffect(() => {
    axios.get(`http://localhost:8080/api/communities/${params.communityId}`, {
      headers: {
        Authorization: `Bearer ${parsedToken.accessToken}`
      }
    })
      .then(response => {
        setCommunity(response.data);
      })
      .catch(error => {
        console.error('Error fetching community:', error);
      });
  }, [params.communityId]);

  const handleUpdate = () => {
    navigate(`/community/update/${params.communityId}`, { state: { community } });
  };

  const handleDelete = async () => {
    if (window.confirm('정말로 삭제하시겠습니까?')) {
      try {
        await axios.delete(`http://localhost:8080/api/communities/${params.communityId}`, {
          headers: { Authorization: `Bearer ${parsedToken.accessToken}` }
        });
        navigate('/community');
      } catch (error) {
        console.error('Error deleting community:', error);
      }
    }
  };

  const loggedInUserId = getCookie('user')?.id;
  const communityWriterId = community?.userId;

  const isWriter = loggedInUserId && loggedInUserId === communityWriterId;

  const formattedDate = community?.createdAt
    ? new Date(community.createdAt).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).replace(/ /g, '')
    : '로딩 중...';

  return (
    <section className="community-list-page-section" id="contact">
      {/* 페이지 사이즈 */}
      <div className="container" style={{ maxWidth: '900px' }}>
        {/* 게시글 */}
        <div className="row gx-4 gx-lg-5 align-items-center" style={{ border: '2px solid #E2E2E2' }}>
          {/* 제목, 작성자 */}
          <div className='community-header' style={{ border: '0px solid #E2E2E2', borderBottom: '2px solid #E2E2E2' }}>
            <div className="col-md-12" style={{ marginTop: '10px' }}>
              <h1 className="fw-bolder">{community?.title || '로딩 중...'}</h1>
              <div className="d-flex justify-content-between" style={{ marginBottom: '10px' }}>
                <p className="lead mb-0" style={{ color: 'black' }}>
                  {community?.writer || '로딩 중...'}
                </p>
                <p className="lead">
                  조회수 {community?.hit || 0} | {community?.category || '로딩 중...'} | {formattedDate}
                </p>
              </div>
            </div>
          </div>
          {community?.images && community.images.map((image, index) => {
            return (
              <div className="col-md-12" key={index} style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '402px', marginTop: '30px' }}>
                <img
                  src={image}
                  alt={`${index}`}
                  style={{
                    maxWidth: '600px',
                    maxHeight: '402px',
                    width: 'auto',
                    height: 'auto',
                    objectFit: community.images.length === 1 && new Image().src === image ? 'contain' : 'cover'
                  }}
                />
              </div>
            );
          })}
          <div className="col-md-12">
            <p className="lead" style={{ color: 'black', marginBottom: '20px', marginTop: '30px'  }}>{community?.contents || '로딩 중...'}</p>
          </div>
          {/* 수정, 삭제 버튼 */}
          {isWriter && (
            <>
              <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '20px' }}>
                <button className="btn " onClick={handleUpdate} style={{ backgroundColor: '#FFFFFF', borderColor: '#999999', color: "#999999", marginRight: '10px' }}>수정</button>
                <button className="btn " onClick={handleDelete} style={{ backgroundColor: '#FFFFFF', borderColor: '#999999', color: "#999999" }}>삭제</button>
              </div>
            </>
          )}
        </div>
        <div style={{ marginTop: '20px' }}>
          <Comment communityId={params.communityId} />
        </div>
      </div>
    </section >
  );
}

export default CommunityDetails;
