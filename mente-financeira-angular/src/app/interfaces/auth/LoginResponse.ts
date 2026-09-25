export interface LoginResponse{
    token : string,
    type : string,
    expiresAt : string,
    userId : number,
    name : string,
    email : string
}