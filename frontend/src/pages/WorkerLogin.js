import React from 'react';
import { useNavigate } from 'react-router-dom';

const WorkerSchedule = () => {
    const navigate = useNavigate();

    return (
        <div style={{ padding: '20px' }}>
            <div style={{
                display: 'flex',
                alignItems: 'center',
                marginBottom: '20px',
                borderBottom: '1px solid #eee',
                paddingBottom: '15px'
            }}>
                <button
                    onClick={() => navigate('/home')}
                    style={{
                        background: 'none',
                        border: 'none',
                        fontSize: '20px',
                        cursor: 'pointer',
                        marginRight: '10px'
                    }}
                >
                    ←
                </button>
                <h2 style={{ margin: 0, fontSize: '20px' }}>작업일정</h2>
            </div>

            <div style={{
                background: '#f5f5f5',
                padding: '20px',
                borderRadius: '10px',
                textAlign: 'center',
                minHeight: '200px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}>
                <p style={{ color: '#666' }}>작업일정이 여기에 표시됩니다.</p>
            </div>
        </div>
    );
};

export default WorkerSchedule;