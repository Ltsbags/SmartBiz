import { Controller, Post, Body, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { RegisterBusinessDto } from './dto/register.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { ApiResponseDto } from '../../common/dto/api-response.dto';

@ApiTags('Authentication')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('register')
  @ApiOperation({ summary: 'Register new Business and Tenant Admin' })
  async register(@Body() dto: RegisterBusinessDto) {
    const data = await this.authService.register(dto);
    return new ApiResponseDto(data, 'Business and Admin user created successfully');
  }

  @Post('login')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'User Authentication & JWT Token Issuance' })
  async login(@Body() dto: LoginDto) {
    const data = await this.authService.login(dto);
    return new ApiResponseDto(data, 'User authenticated successfully');
  }

  @Post('refresh')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Rotate Refresh Token and Issue New Access Token' })
  async refreshToken(@Body() dto: RefreshTokenDto) {
    const data = await this.authService.refreshToken(dto);
    return new ApiResponseDto(data, 'Token rotated successfully');
  }
}
