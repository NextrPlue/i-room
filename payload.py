import struct

# 전송할 값들
data = struct.pack(">dddqddq",  # big-endian 직렬화

    35.8893823,   # latitude
    128.6064844,  # longitude


    80,        # heartRate

    0,        # steps
    0,        # speed
    0,         # pace
    0           # stepPerMinute
)

with open("payload.bin", "wb") as f:
    f.write(data)

print("payload.bin 생성 완료:", len(data), "bytes")
